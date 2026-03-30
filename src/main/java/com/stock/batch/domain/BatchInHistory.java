package com.stock.batch.domain;

import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

/*
hist_id     	bigint(20)	    NO	PRI		auto_increment  // 배치 실행 이력 고유 ID (PK)
job_id	        bigint(20)	    NO	MUL                     // 실행된 배치 작업 ID (batch_in.job_id FK)
job_name	    varchar(100)	NO                          // 배치 작업 이름 (실행 시점 기준 스냅샷)
job_info	    varchar(255)	YES                         // 배치 작업 설명
exec_start_time	datetime	    NO                          // 배치 실행 시작 시간
exec_end_time	datetime	    YES                         // 배치 실행 종료 시간
exec_status	    varchar(20)	    NO                          // 실행 상태 (예: SUCCESS, FAIL)
exec_message	text	        YES                         // 실행 결과 메시지 또는 오류 메시지
exec_date	    date	        NO                          // 실행 기준 날짜 (배치 집계용)
duration_ms 	bigint(20)	    YES                         // 실행 소요 시간 (밀리초)
*/

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchInHistory {

    // 배치 실행 이력 고유 ID (PK)
    private Long histId;

    // 실행된 배치 작업 ID
    private Integer jobId;

    // 배치 작업 이름
    private String jobName;

    // 배치 작업 설명
    private String jobInfo;

    // 배치 실행 시작 시간
    private Timestamp execStartTime;

    // 배치 실행 종료 시간
    private Timestamp execEndTime;

    // 실행 상태 (SUCCESS / FAIL)
    private String execStatus;

    // 실행 메시지 또는 에러 메시지
    private String execMessage;

    // 실행 기준 날짜 (일 단위 조회용)
    private Date execDate;

    // 실행 소요 시간 (밀리초)
    private Long durationMs;
}