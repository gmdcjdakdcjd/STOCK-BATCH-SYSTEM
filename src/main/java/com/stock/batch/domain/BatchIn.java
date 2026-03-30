package com.stock.batch.domain;

import lombok.*;

import java.sql.Timestamp;

/*
job_id	        bigint(20)	    NO	    PRI		auto_increment      // 배치 작업 고유 ID (PK, 자동 증가)
job_name	    varchar(100)	NO                                  // 배치 작업 이름
job_info	    varchar(255)	YES                                 // 배치 작업 설명 또는 추가 정보
schedule_gb	    varchar(1)	    NO                                  // 스케줄 구분 (예: M=월, D=일, W=주 등)
job_month	    varchar(2)	    YES                                 // 실행 월 (월 단위 스케줄일 경우 사용)
job_day	        varchar(2)	    YES                                 // 실행 일 (일 단위 스케줄일 경우 사용)
job_week	    varchar(3)	    YES                                 // 실행 요일 (예: MON, TUE 등)
job_hour	    varchar(4)	    YES                                 // 실행 시간 (시)
job_min	        varchar(4)	    YES                                 // 실행 분
act_gb	        varchar(1)	    YES                                 // 동작 구분 (예: IN/OUT 등 배치 유형)
last_exec_info	varchar(50)	    YES                                 // 마지막 실행 정보 (시간 또는 상태 등)
next_exec_info	varchar(50)	    YES                                 // 다음 실행 예정 정보
file_pattern	varchar(255)	NO                                  // 처리할 파일 패턴 (예: DAILY_PRICE_*.txt)
is_active	    tinyint(1)	    YES		1                           // 활성 여부 (1=사용, 0=미사용)
created_at	    datetime	    YES		        current_timestamp() // 생성 시각
*/

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchIn implements SchedulableJob{

    // 배치 작업 고유 ID (DB PK)
    private Integer jobId;

    // 배치 작업 이름
    private String jobName;

    // 배치 작업 설명
    private String jobInfo;

    // 스케줄 구분 (월/일/주 등 실행 기준)
    private String scheduleGb;

    // 실행 월
    private String jobMonth;

    // 실행 일
    private String jobDay;

    // 실행 요일
    private String jobWeek;

    // 실행 시간 (시)
    private String jobHour;

    // 실행 분
    private String jobMin;

    // 배치 동작 구분
    private String actGb;

    // 마지막 실행 정보
    private String lastExecInfo;

    // 다음 실행 예정 정보
    private String nextExecInfo;

    // 처리할 파일 패턴
    private String filePattern;

    // 활성 여부 (1=활성, 0=비활성)
    private Integer isActive;

    // 생성 시각
    private Timestamp createdAt;
}