package com.stock.batch.domain;

import lombok.*;

import java.sql.Timestamp;

/*
job_id	        bigint(20)	    NO	PRI		auto_increment  // 배치 작업 고유 ID (PK)
job_name	    varchar(100)	NO                          // 배치 작업 이름
job_info	    varchar(255)	YES                         // 배치 작업 설명
schedule_gb	    varchar(1)	    NO                          // 스케줄 구분 (D=Daily, W=Weekly, M=Monthly)
job_month	    varchar(2)	    YES                         // 실행 월 (01~12, 월 단위 스케줄일 경우)
job_day	        varchar(2)	    YES                         // 실행 일 (01~31, 일 단위 또는 월 단위 스케줄)
job_week	    varchar(3)	    YES                         // 실행 요일 (MON~SUN, 주 단위 스케줄)
job_hour	    varchar(4)	    YES                         // 실행 시간 (HH)
job_min	        varchar(4)	    YES                         // 실행 분 (MM)
act_gb	        varchar(1)	    YES                         // 동작 여부 (Y=실행, N=미실행)
last_exec_info	varchar(50)	    YES                         // 마지막 실행 정보 (예: 실행 날짜 또는 상태)
next_exec_info	varchar(50)	    YES                         // 다음 실행 예정 정보
shell_file_dir	varchar(255)	YES                         // 파이썬 파일 경로
is_active	    tinyint(1)	    YES		1                   // 활성 여부 (1=사용, 0=미사용)
created_at	    datetime	    YES		current_timestamp() // 생성 시각
*/

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOut implements SchedulableJob{

    // 배치 작업 고유 ID
    private Integer jobId;

    // 배치 작업 이름
    private String jobName;

    // 배치 작업 설명
    private String jobInfo;

    // 스케줄 구분 (D=daily, W=weekly, M=monthly)
    private String scheduleGb;

    // 실행 월 (01~12)
    private String jobMonth;

    // 실행 일 (01~31)
    private String jobDay;

    // 실행 요일 (MON~SUN)
    private String jobWeek;

    // 실행 시간 (HH)
    private String jobHour;

    // 실행 분 (MM)
    private String jobMin;

    // 동작 여부 (Y / N)
    private String actGb;

    // 마지막 실행 정보 (yyyy-MM-dd 등)
    private String lastExecInfo;

    // 다음 실행 예정 정보 (yyyy-MM-dd 등)
    private String nextExecInfo;

    // 배치 실행 파일 경로 (Shell, Script 등)
    private String shellFileDir;

    // 사용 여부
    private Integer isActive;

    // 생성일
    private Timestamp createdAt;
}