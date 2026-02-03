package com.stock.batch.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StockJobInfo {
    private String jobCode;
    private String handlerName;
    private String jobName;
    private String useYn;
    private LocalDateTime regDt;
}
