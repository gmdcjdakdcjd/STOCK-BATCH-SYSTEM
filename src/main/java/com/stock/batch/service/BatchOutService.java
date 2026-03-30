package com.stock.batch.service;

import com.stock.batch.domain.BatchOut;
import com.stock.batch.domain.StockJobQueue;
import com.stock.batch.mapper.BatchOutMapper;
import com.stock.batch.mapper.StockJobQueueMapper;
import com.stock.batch.util.BatchScheduleCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOutService {

    private final BatchOutMapper batchOutMapper;
    private final StockJobQueueMapper stockJobWaitingMapper;

    /**
     * 스케줄에 따라 실행 대상 BatchOut 조회
     * → 실행 요청(stock_job_waiting) 생성
     */
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

        log.info("[BatchOut] 실행 대상 BatchOut = {}", jobs.size());

        if (jobs.isEmpty()) {
            log.debug("[BatchOut] 실행 대상 없음");
            return;
        }

        for (BatchOut job : jobs) {
            log.info(
                    "[BatchOut] 실행 요청 생성: jobId={}, jobName={}, jobCode={}",
                    job.getJobId(),
                    job.getJobName(),
                    job.getShellFileDir()
            );
            createExecutionRequest(job);
        }
    }

    /**
     * 실제 실행 X
     * 실행 요청만 생성 O
     */
    private void createExecutionRequest(BatchOut job) {

        // BatchOut 실행 요청을 Queue(stock_job_queue)에 등록
        // 실제 배치 실행은 별도의 Worker에서 처리
        // status = W (Waiting)
        StockJobQueue waiting = StockJobQueue.builder()
                .jobCode(job.getShellFileDir())   // ex) KospiUpdate
                .status("W")
                .batchOutId(job.getJobId())
                .build();

        // StockJobQueue 객체를 DB에 INSERT하여 배치 실행 요청 등록
        stockJobWaitingMapper.insertWaiting(waiting);

        log.info(
                "[BatchOut] stock_job_waiting INSERT 완료: jobId={}, jobCode={}",
                job.getJobId(),
                job.getShellFileDir()
        );

        // BatchOut 실행 상태 갱신 (Java Batch의 책임)
        // BatchOut 실행 상태 갱신
        // act_gb = 'Y' : 오늘 실행 처리됨
        // last_exec_info : 마지막 실행 날짜
        // next_exec_info : 다음 실행 예정 날짜 계산
        job.setActGb("Y");
        job.setLastExecInfo(LocalDate.now().toString());
        job.setNextExecInfo(
                BatchScheduleCalculator
                        .calculateNextExecDate(job, LocalDate.now())
                        .toString()
        );

        // 실행 처리된 BatchOut의 상태 및 다음 실행 예정일 DB 업데이트
        batchOutMapper.updateExecutionStatus(job);

        log.debug(
                "[BatchOut] BatchOut 상태 업데이트 완료: jobId={}, actGb=Y, nextExec={}",
                job.getJobId(),
                job.getNextExecInfo()
        );
    }

    /**
     * 매일 자정 이후 실행 상태 초기화
     */
    public void resetActGb() {
        batchOutMapper.resetAllActGb();
        log.info("[BatchOut] act_gb reset completed");
    }

    /**
     * 서버 기동 시 전일 실행 상태 정리
     */
    public void startupInit() {
        int cnt = batchOutMapper.resetActGbByLastExecBeforeToday();
        log.info("[BatchOut] startup act_gb reset done (count={})", cnt);
    }

    private String format(int v) {
        return String.format("%02d", v);
    }
}
