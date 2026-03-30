package com.stock.batch.scheduler;

import com.stock.batch.service.BatchOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchOutScheduler {

    private final BatchOutService batchOutService;

    /**
     * 매 1분마다 실행
     * 현재 시간(job_hour, job_min)과 스케줄 조건(D/W/M/Y)이 맞는
     * BatchOut 작업(act_gb='N', is_active=1)을 조회하여 실행
     */
    @Scheduled(fixedDelay = 60000)
    public void tick() {
        log.info("BatchOutScheduler tick");
        batchOutService.executeDueJobs();
    }

    /**
     * 매일 자정 BatchOut 실행 상태 초기화
     * 다음 날 배치가 다시 실행될 수 있도록 act_gb 값을 'N'으로 리셋
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightReset() {
        batchOutService.resetActGb();
    }
}
