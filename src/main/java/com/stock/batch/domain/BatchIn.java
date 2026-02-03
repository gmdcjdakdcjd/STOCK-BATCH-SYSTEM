package com.stock.batch.domain;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchIn implements SchedulableJob{

    private Integer jobId;
    private String jobName;
    private String jobInfo;

    private String scheduleGb;

    private String jobMonth;
    private String jobDay;
    private String jobWeek;
    private String jobHour;
    private String jobMin;

    private String actGb;

    private String lastExecInfo;
    private String nextExecInfo;

    private String filePattern;

    private Integer isActive;

    private Timestamp createdAt;
}
