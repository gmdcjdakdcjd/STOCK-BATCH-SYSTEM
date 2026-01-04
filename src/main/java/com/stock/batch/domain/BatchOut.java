package com.stock.batch.domain;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOut  implements SchedulableJob{

    private Integer jobId;

    private String jobName;
    private String jobInfo;

    private String scheduleGb;   // D=daily, W=weekly, M=monthly

    private String jobMonth;     // 01~12
    private String jobDay;       // 01~31
    private String jobWeek;      // MON~SUN

    private String jobHour;      // HH
    private String jobMin;       // MM

    private String actGb;        // Y / N

    private String lastExecInfo; // yyyy-MM-dd
    private String nextExecInfo; // yyyy-MM-dd

    private String shellFileDir; // 배치 실행 파일 경로

    private Integer isActive;    // 사용 여부

    private Timestamp createdAt; // 생성일
}
