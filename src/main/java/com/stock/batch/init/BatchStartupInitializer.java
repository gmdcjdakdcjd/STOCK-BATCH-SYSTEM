package com.stock.batch.init;

import com.stock.batch.service.BatchInService;
import com.stock.batch.service.BatchOutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStartupInitializer implements ApplicationRunner {

    private final BatchInService batchInService;
    private final BatchOutService batchOutService;

    @Override
    public void run(ApplicationArguments args) {

        log.info("[Batch] startup initialization begin");

        batchInService.startupInit();
        batchOutService.startupInit();

        log.info("[Batch] startup initialization complete");
    }
}
