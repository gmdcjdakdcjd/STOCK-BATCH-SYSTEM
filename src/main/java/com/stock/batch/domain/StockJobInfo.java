package com.stock.batch.domain;

import java.time.LocalDateTime;
import lombok.Data;

/*
job_code	    varchar(100)	NO	PRI                     // 배치 작업 코드 (PK)
handler_name	varchar(200)	YES                         // 실행할 핸들러 클래스 또는 스크립트 경로
description	    varchar(200)	YES                         // 배치 작업 설명
use_yn	        char(1)	        YES		Y                   // 사용 여부 (Y=사용, N=미사용)
reg_dt	        datetime	    YES		current_timestamp() // 등록 일시
*/

@Data
public class StockJobInfo {

    // 배치 작업 코드 (고유 식별자)
    private String jobCode;

    // 실행 핸들러 이름 또는 스크립트 경로
    private String handlerName;

    // 배치 작업 설명
    private String jobName;

    // 사용 여부 (Y / N)
    private String useYn;

    // 등록 일시
    private LocalDateTime regDt;
}