package com.stock.batch.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockJobQueue {

    private Long waitId;
    private String jobCode;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private int batchOutId;
}
