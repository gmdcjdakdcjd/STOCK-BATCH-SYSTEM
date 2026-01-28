package com.stock.batch.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchInJobExecutor {

    private final BatchInPreCleaner preCleaner;
    private final BatchInProcessor processor;

    public BatchInJobExecutor(
            BatchInPreCleaner preCleaner,
            BatchInProcessor processor
    ) {
        this.preCleaner = preCleaner;
        this.processor = processor;
    }

    public BatchInProcessor.BatchResult execute(
            String jobName,
            String today,
            String pattern
    ) throws Exception {

        log.info("[BatchInJobExecutor] START jobName={}", jobName);

        // 대표 job에서만 truncate
        preCleaner.clean(jobName);

        BatchInProcessor.BatchResult result =
                processor.executeBatch(today, pattern);

        log.info("[BatchInJobExecutor] END jobName={}", jobName);

        return result;
    }


}
