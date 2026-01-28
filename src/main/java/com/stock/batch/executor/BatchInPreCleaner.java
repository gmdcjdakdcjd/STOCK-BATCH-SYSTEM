package com.stock.batch.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
public class BatchInPreCleaner {

    private final DataSource dataSource;

    public BatchInPreCleaner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * jobName 기준 pre-clean
     * - 그룹당 "대표 job"에서만 truncate
     */
    public void clean(String jobName) {

        // =========================
        // KODEX (SUMMARY에서만)
        // =========================
        if (jobName.equals("KODEX_ETF_SUMMARY_IN")) {
            truncate(
                    "kodex_etf_summary",
                    "kodex_etf_holdings"
            );
            return;
        }

        // =========================
        // NPS (HEADER에서만)
        // =========================
        if (jobName.equals("NPS_PORTFOLIO_HEADER_IN")) {
            truncate(
                    "nps_portfolio_header",
                    "nps_portfolio_item"
            );
            return;
        }

        log.info("[BatchInPreCleaner] skip pre-clean for jobName={}", jobName);
    }

    private void truncate(String... tables) {

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String table : tables) {
                log.info("[BatchInPreCleaner] TRUNCATE {}", table);
                stmt.execute("TRUNCATE TABLE " + table);
            }

        } catch (Exception e) {
            throw new RuntimeException("[BatchInPreCleaner] truncate failed", e);
        }
    }
}
