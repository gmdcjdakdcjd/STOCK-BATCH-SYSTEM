package com.stock.batch.domain;

public interface SchedulableJob {

    String getScheduleGb();
    String getJobDay();
    String getJobWeek();
}
