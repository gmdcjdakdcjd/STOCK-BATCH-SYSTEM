package com.stock.batch.service;

import com.stock.batch.domain.BatchIn;
import com.stock.batch.domain.BatchInHistory;
import com.stock.batch.executor.BatchInJobExecutor;
import com.stock.batch.executor.BatchInProcessor;
import com.stock.batch.mapper.BatchInHistoryMapper;
import com.stock.batch.mapper.BatchInMapper;
import com.stock.batch.util.BatchScheduleCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchInService {

    private final BatchInMapper batchInMapper;
    private final BatchInHistoryMapper historyMapper;
//    private final BatchInProcessor processor;
    private final BatchInJobExecutor batchInJobExecutor;

    // ===============================
    // 실행 대상 스캔 (BatchOut과 동일 패턴)
    // ===============================
    public void executeDueJobs() {

        LocalDateTime now = LocalDateTime.now();
        log.info("[BatchIn] scan start at {}", now);

        String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List<BatchIn> jobs =
                batchInMapper.selectExecutableBatchIn(
                        format(now.getMonthValue()),
                        format(now.getDayOfMonth()),
                        now.getDayOfWeek().name().substring(0, 3),
                        format(now.getHour()),
                        format(now.getMinute())
                );

        log.info("[BatchIn] 실행 대상 BatchIn = {}", jobs.size());

        if (jobs.isEmpty()) {
            log.debug("[BatchIn] 실행 대상 없음");
            return;
        }

        for (BatchIn job : jobs) {
            log.info("실행 시작: jobId={}, jobName={}", job.getJobId(), job.getJobName());
            executeJob(job, today);
        }
    }

    // ===============================
    // 단일 Job 실행
    // ===============================
    private void executeJob(BatchIn job, String today) {

        long start = System.currentTimeMillis();
        String status = "SUCCESS";
        String execMessage = "NO_ERROR";

        try {
//            BatchInProcessor.BatchResult result =
//                    processor.executeBatch(today, job.getFilePattern());

            BatchInProcessor.BatchResult result =
                    batchInJobExecutor.execute(
                            job.getJobName(),
                            today,
                            job.getFilePattern()
                    );

            // 이동 전용 Job은 파일 수로 판단하지 않는다
            if (job.getJobName().startsWith("FOLDER_MOVE")) {
                execMessage = "MOVE_SUCCESS";

            } else {
                if (result.total() == 0) {
                    status = "FAIL";
                    execMessage = "NO_FILES";
                } else if (result.fail() > 0) {
                    execMessage = "PARTIAL_FAIL(" + result.fail() + ")";
                }
            }

        } catch (Exception e) {
            status = "FAIL";
            execMessage = "SYSTEM_ERROR";
            log.error("[BatchIn] 실행 실패: jobId={}", job.getJobId(), e);
        }

        long end = System.currentTimeMillis();

        BatchInHistory history = BatchInHistory.builder()
                .jobId(job.getJobId())
                .jobName(job.getJobName())
                .jobInfo(job.getJobInfo())
                .execStartTime(new Timestamp(start))
                .execEndTime(new Timestamp(end))
                .execStatus(status)
                .execMessage(execMessage)
                .execDate(Date.valueOf(LocalDate.now()))
                .durationMs(end - start)
                .build();

        historyMapper.insertBatchInHistory(history);

        job.setActGb("Y");
        job.setLastExecInfo(LocalDate.now().toString());
        job.setNextExecInfo(
                BatchScheduleCalculator
                        .calculateNextExecDate(job, LocalDate.now())
                        .toString()
        );

        batchInMapper.updateExecutionStatus(job);
    }



    // ===============================
    // act_gb 리셋 (BatchOut과 대칭)
    // ===============================
    public void resetActGb() {
        batchInMapper.resetAllActGb();
    }

    private String format(int v) {
        return String.format("%02d", v);
    }

    public void startupInit() {

        int cnt = batchInMapper.resetActGbByLastExecBeforeToday();

        log.info("[BatchOut] startup act_gb reset done (count={})", cnt);
    }

}
