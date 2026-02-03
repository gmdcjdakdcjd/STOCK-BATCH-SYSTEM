package com.stock.batch.domain;

import lombok.*;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchOutHistory {

    private Long histId;

    private Integer jobId;
    private String jobName;
    private String jobInfo;

    private Timestamp execStartTime;
    private Timestamp execEndTime;
    private String execStatus;
    private String execMessage;

    private Date execDate;
    private Long durationMs;
}
