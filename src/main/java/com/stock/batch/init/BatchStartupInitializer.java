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

        // 서버 기동 시 배치 상태 초기화
        // 서버 장애 또는 비정상 종료로 인해 남아있을 수 있는
        // BatchIn / BatchOut 실행 상태(act_gb)를 정리
        batchInService.startupInit();
        batchOutService.startupInit();

        log.info("[Batch] startup initialization complete");
    }
}
