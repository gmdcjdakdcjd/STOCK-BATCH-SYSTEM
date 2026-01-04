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
     * 1분마다 실행
     */
    @Scheduled(fixedDelay = 60000)
    public void tick() {
        log.info("BatchInScheduler tick");
        batchInService.executeDueJobs();
    }

    /**
     * 매일 자정 act_gb 리셋
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightReset() {
        batchInService.resetActGb();
    }
}
