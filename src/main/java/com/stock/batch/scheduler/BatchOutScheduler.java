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
     * 1분마다 실행
     */
    @Scheduled(fixedDelay = 60000)
    public void tick() {
        log.info("BatchOutScheduler tick");
        batchOutService.executeDueJobs();
    }

    /**
     * 매일 자정 act_gb 리셋
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightReset() {
        batchOutService.resetActGb();
    }
}
