package com.stock.batch.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BatchInProcessor {

    private final DataSource dataSource;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${batch.path.in}")
    private String batchInRoot;

    @Value("${batch.path.out}")
    private String batchOutRoot;

    @Value("${batch.path.archive}")
    private String archiveRoot;

    @Value("${batch.path.error}")
    private String errorRoot;

    public record BatchResult(int total, int success, int fail) {}

    public BatchInProcessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // =================================================
    // 1) 파일 필터링
    // =================================================
    public List<Path> getFiles(String today, String pattern) throws Exception {

        Path folder = Paths.get(batchInRoot, today);

        if (!Files.isDirectory(folder)) {
            log.debug("[BatchIn] folder not found: {}", folder);
            return List.of();
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                folder,
                p -> p.getFileName().toString().startsWith(pattern)
        )) {
            List<Path> files = new ArrayList<>();
            stream.forEach(files::add);
            return files;
        }
    }

    // =================================================
    // 파일명 → 테이블명 매핑
    // =================================================
    private String resolveTableName(String fileName) {

        // 확장자 제거
        String base = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf("."))
                : fileName;

        base = base.toLowerCase();

        // 날짜 제거 (_yyyyMMdd)
        base = base.replaceAll("_\\d{8}$", "");

        // strategy_result / strategy_detail 만 국가코드 제거
        if (base.startsWith("strategy_result") || base.startsWith("strategy_detail")) {
            base = base.replaceAll("_(kr|us)$", "");
        }

        // 4. DAILY_PRICE_PHYSICAL / DAILY_PRICE_STOCKINDEX 계열
        // DAILY_PRICE_PHYSICAL / DAILY_PRICE_STOCKINDEX 계열
        if (base.startsWith("daily_price_physical")) {
            base = "daily_price_physical";
        }
        else if (base.startsWith("daily_price_stockindex")) {
            base = "daily_price_stockindex";
        }

        return base;
    }

    // =================================================
    // 2) 단일 파일 처리
    // =================================================
    public void processFile(Path filePath) throws Exception {

        String fileName = filePath.getFileName().toString();
        String tableName = resolveTableName(fileName);

        if (fileName.endsWith(".csv")) {
            processCsv(filePath, tableName);
        } else if (fileName.endsWith(".json")) {
            processJson(filePath, tableName);
        } else if (fileName.endsWith(".txt")) {
            processTxt(filePath, tableName);
        } else {
            log.warn("[BatchIn] unsupported file skipped: {}", fileName);
        }
    }

    // =================================================
    // 3) JSON 처리
    // =================================================
    private void processJson(Path filePath, String tableName) throws Exception {

        try (BufferedReader reader =
                     Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {

            Object data = mapper.readValue(reader, Object.class);

            if (data instanceof List<?> list) {
                for (Object o : list) {
                    insertDynamic(tableName, (Map<String, Object>) o);
                }
            } else {
                insertDynamic(tableName, (Map<String, Object>) data);
            }
        }
    }

    // =================================================
    // 4) CSV 처리
    // =================================================
    private void processCsv(Path filePath, String tableName) throws Exception {

        try (
                BufferedReader reader =
                        Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
                CSVParser parser =
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim()
                                .parse(reader);
                Connection conn = dataSource.getConnection()
        ) {
            batchInsert(parser, tableName, conn);
        }
    }

    // =================================================
    // 5) TXT 처리 (| 구분자)
    // =================================================
    private void processTxt(Path filePath, String tableName) throws Exception {

        try (
                BufferedReader reader =
                        Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
                CSVParser parser =
                        CSVFormat.DEFAULT
                                .withDelimiter('|')
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim()
                                .parse(reader);
                Connection conn = dataSource.getConnection()
        ) {
            batchInsert(parser, tableName, conn);
        }
    }

    // =================================================
    // 6) CSV/TXT 공통 batch insert
    // =================================================
    private void batchInsert(CSVParser parser, String tableName, Connection conn) throws Exception {

        boolean hasRowError = false;
        conn.setAutoCommit(false);

        List<String> headers = parser.getHeaderNames().stream()
                .map(String::trim)
                .collect(Collectors.toList());

        String sql = "REPLACE INTO " + tableName +
                " (" + String.join(", ", headers) + ") VALUES (" +
                headers.stream().map(h -> "?").collect(Collectors.joining(", ")) + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            int count = 0;

            for (CSVRecord record : parser) {

                if (record.size() != headers.size()) {
                    log.error(
                            "[BatchIn] column mismatch: expected={}, actual={}, record={}",
                            headers.size(),
                            record.size(),
                            record
                    );
                    hasRowError = true;
                    continue;
                }

                for (int i = 0; i < headers.size(); i++) {
                    setPreparedValue(
                            ps,
                            i + 1,
                            tableName,
                            headers.get(i),
                            record.get(i)
                    );
                }

                ps.addBatch();

                if (++count % 1000 == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();

            // 여기서 정책 결정
            if (hasRowError) {
                conn.rollback();   // ← DB 반영 안함
                throw new RuntimeException(
                        "[BatchIn] row error exists (column mismatch)"
                );
            }

            conn.commit();          // ← row 에러 없을 때만 반영

            log.info(
                    "[BatchIn] file insert completed: table={}, rows={}",
                    tableName,
                    count
            );
        }
    }


    // =================================================
    // 7) 컬럼 타입 판단 (테이블별 룰 포함)
    // =================================================
    private void setPreparedValue(
            PreparedStatement ps,
            int idx,
            String tableName,
            String header,
            String raw
    ) throws Exception {

        if (raw == null) {
            ps.setObject(idx, null);
            return;
        }

        String cleaned = raw.trim();

        if (
                cleaned.isEmpty()
                        || cleaned.equalsIgnoreCase("none")
                        || cleaned.equalsIgnoreCase("null")
                        || cleaned.equalsIgnoreCase("nan")
        ) {
            ps.setObject(idx, null);
            return;
        }

        String col = header.toLowerCase();
        String tbl = tableName.toLowerCase();

        // kodex_etf_* 전용 base_date 처리
        if (col.equals("base_date") && tbl.startsWith("kodex_etf")) {
            ps.setString(idx, cleaned.replace(".", ""));
            return;
        }

        // 코드 계열은 문자열 고정
        if (col.equals("code") || col.equals("stock_code")) {
            ps.setString(idx, cleaned);
            return;
        }

        try {
            ps.setDouble(idx, Double.parseDouble(cleaned.replace(",", "")));
        } catch (NumberFormatException e) {
            ps.setString(idx, cleaned);
        }
    }

    // =================================================
    // 8) JSON 단건 insert
    // =================================================
    private void insertDynamic(String tableName, Map<String, Object> json) throws Exception {

        List<String> columns = new ArrayList<>(json.keySet());

        String sql = "REPLACE INTO " + tableName +
                " (" + String.join(", ", columns) + ") VALUES (" +
                columns.stream().map(c -> "?").collect(Collectors.joining(", ")) + ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            for (String col : columns) {
                ps.setObject(idx++, json.get(col));
            }

            ps.executeUpdate();
        }
    }

    // =================================================
    // 9) 폴더 이동
    // =================================================
    public void moveTodayFolder(String today) {

        Path src = Paths.get(batchOutRoot, today);
        Path dest = Paths.get(batchInRoot, today);

        try {
            Files.createDirectories(dest);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Files.move(
                                file,
                                dest.resolve(file.getFileName()),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("[BatchIn] folder move failed", e);
        }
    }

    public BatchResult executeBatch(String today, String pattern) throws Exception {

        // 1. BATCH_OUT → BATCH_IN (MOVE)
        moveTodayFolder(today);

        List<Path> files = getFiles(today, pattern);

        int success = 0;
        int fail = 0;

        for (Path batchInFile : files) {

            boolean ok = true;

            try {
                // 2. DB 처리 (BATCH_IN 기준)
                processFile(batchInFile);

                log.info(
                        "[BatchIn] NO_ERROR file={}",
                        batchInFile.getFileName()
                );

            } catch (Exception e) {

                ok = false;

                log.error(
                        "[BatchIn] FILE_ERROR file={}",
                        batchInFile.getFileName(),
                        e
                );
            }

            // 3. 결과에 따라 BATCH_IN → ARCHIVE / ERROR (MOVE)
            moveFileByResult(batchInFile, today, ok);

            if (ok) {
                success++;
            } else {
                fail++;
            }
        }

        return new BatchResult(files.size(), success, fail);
    }


    private void moveFileByResult(Path src, String today, boolean success) {

        Path destDir = success
                ? Paths.get(archiveRoot, today)
                : Paths.get(errorRoot, today);

        try {
            Files.createDirectories(destDir);

            Files.move(
                    src,
                    destDir.resolve(src.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.info(
                    "[BatchIn] file moved: {} → {}",
                    src.getFileName(),
                    success ? "ARCHIVE" : "ERROR"
            );

        } catch (Exception e) {
            log.error("[BatchIn] file move failed", e);
        }
    }



}
