package com.stock.batch.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/*
wait_id	        bigint(20)	NO	    PRI		                    auto_increment   // 배치 대기열 고유 ID (PK)
job_code	    varchar(200)	    YES                                          // 실행 요청된 배치 작업 코드 (stock_job_info.job_code)
status	        char(1)	        YES                                             // 처리 상태 (예: W=대기, R=실행중, F=실패, S=성공)
requested_at	datetime	    NO		        current_timestamp()             // 배치 실행 요청 시각
started_at	    datetime	    YES                                             // 실제 실행 시작 시각
batch_out_id	bigint(20)	    YES                                             // 실행된 batch_out 작업 ID (연동용)
*/

@Data
@Builder
public class StockJobQueue {

    // 배치 대기열 고유 ID
    private Long waitId;

    // 실행 요청된 배치 작업 코드
    private String jobCode;

    // 처리 상태 (W=Waiting, R=Running, S=Success, F=Fail 등)
    private String status;

    // 배치 실행 요청 시각
    private LocalDateTime requestedAt;

    // 실제 실행 시작 시각
    private LocalDateTime startedAt;

    // 실행된 BatchOut 작업 ID
    private int batchOutId;
}