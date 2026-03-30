package com.stock.batch.scheduler;

import com.stock.batch.service.BatchInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchInScheduler {

    private final BatchInService batchInService;

    /**
     * 매 1분마다 실행
     * 현재 시간(job_hour, job_min)과 스케줄 조건(D/W/M/Y)이 맞는
     * BatchIn 작업(act_gb='N', is_active=1)을 조회하여 실행
     */
    @Scheduled(fixedDelay = 60000)
    public void tick() {
        log.info("BatchInScheduler tick");
        batchInService.executeDueJobs();
    }

    /**
     * 매일 자정 BatchIn 실행 상태 초기화
     * 다음 날 배치가 다시 실행될 수 있도록 act_gb 값을 'N'으로 리셋
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightReset() {
        batchInService.resetActGb();
    }
}
