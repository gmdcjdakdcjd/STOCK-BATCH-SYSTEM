package com.stock.batch.domain;

import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchInHistory {

    private Long histId;

    private Integer jobId;

    private String jobName;
    private String jobInfo;

    private Timestamp execStartTime;
    private Timestamp execEndTime;

    private String execStatus;     // SUCCESS / FAIL
    private String execMessage;

    private Date execDate;
    private Long durationMs;
}
