package com.stock.batch.service;

import com.stock.batch.domain.BatchOut;
import com.stock.batch.domain.BatchOutHistory;
import com.stock.batch.domain.SchedulableJob;
import com.stock.batch.executor.PythonBatchJob;
import com.stock.batch.mapper.BatchOutHistoryMapper;
import com.stock.batch.mapper.BatchOutMapper;
import com.stock.batch.util.BatchScheduleCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOutService {

    private final BatchOutMapper batchOutMapper;
    private final BatchOutHistoryMapper historyMapper;
    private final PythonBatchJob pythonBatchJob;

    public void executeDueJobs() {

        LocalDateTime now = LocalDateTime.now();
        log.info("[BatchOut] scan start at {}", now);

        List<BatchOut> jobs =
                batchOutMapper.selectExecutableBatchOut(
                        format(now.getMonthValue()),
                        format(now.getDayOfMonth()),
                        now.getDayOfWeek().name().substring(0, 3),
                        format(now.getHour()),
                        format(now.getMinute())
                );

        log.info("[BatchOut]실행 대상 BatchOut = {}", jobs.size());

        if (jobs.isEmpty()) {
            log.debug("[BatchOut] 실행 대상 없음");
            return;
        }
        for (BatchOut job : jobs) {
            log.info("실행 시작: jobId={}, jobName={}", job.getJobId(), job.getJobName());
            executeJob(job);
        }
    }

    private void executeJob(BatchOut job) {

        long start = System.currentTimeMillis();

        var result = pythonBatchJob.runPythonScript(job.getShellFileDir());

        long end = System.currentTimeMillis();

        log.info(
                "실행 완료: jobId={}, status={}, duration={}ms",
                job.getJobId(),
                result.getStatus(),
                (end - start)
        );

        if (!"SUCCESS".equals(result.getStatus())) {
            log.error(
                    "Batch 실패: jobId={}, error={}",
                    job.getJobId(),
                    result.getErrorMsg()
            );
        }

        String status = "SUCCESS".equals(result.getStatus())
                ? "SUCCESS"
                : "FAIL";

        String execMessage =
                "SUCCESS".equals(status)
                        ? "NO_ERROR"
                        : (result.getErrorMsg() != null ? result.getErrorMsg() : "UNKNOWN_ERROR");


        // history 저장
        BatchOutHistory history = BatchOutHistory.builder()
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

        historyMapper.insertBatchOutHistory(history);
        log.debug("BatchOut history saved: jobId={}, histId={}", job.getJobId(), history.getHistId());

        // 상태 업데이트
        job.setActGb("Y");
        job.setLastExecInfo(LocalDate.now().toString());
        job.setNextExecInfo(
                BatchScheduleCalculator.calculateNextExecDate(job, LocalDate.now()).toString()
        );

        batchOutMapper.updateExecutionStatus(job);
        log.debug("BatchOut status updated: jobId={}, actGb=Y, nextExec={}", job.getJobId(), job.getNextExecInfo());
    }

    public void resetActGb() {
        batchOutMapper.resetAllActGb();
    }

    private String format(int v) {
        return String.format("%02d", v);
    }

    public void startupInit() {

        int cnt = batchOutMapper.resetActGbByLastExecBeforeToday();

        log.info("[BatchOut] startup act_gb reset done (count={})", cnt);
    }


}
