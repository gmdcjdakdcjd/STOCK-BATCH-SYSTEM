package com.stock.batch.util;

import com.stock.batch.domain.BatchOut;
import com.stock.batch.domain.SchedulableJob;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class BatchScheduleCalculator {

    public static LocalDate calculateNextExecDate(SchedulableJob job, LocalDate today) {

        return switch (job.getScheduleGb()) {

            // ======================
            // MONTHLY
            // ======================
            case "M" -> {
                int day = Integer.parseInt(job.getJobDay());

                LocalDate base = today.withDayOfMonth(1);
                int lastDayOfMonth = base.lengthOfMonth();

                int execDay = Math.min(day, lastDayOfMonth);
                LocalDate target = base.withDayOfMonth(execDay);

                yield today.isBefore(target)
                        ? target
                        : base.plusMonths(1)
                        .withDayOfMonth(
                                Math.min(day, base.plusMonths(1).lengthOfMonth())
                        );
            }


            // ======================
            // WEEKLY
            // ======================
            case "W" -> {
                DayOfWeek target = toDayOfWeek(job.getJobWeek());
                LocalDate next = today.plusDays(1);

                while (next.getDayOfWeek() != target) {
                    next = next.plusDays(1);
                }
                yield next;
            }

            // ======================
            // DAILY
            // ======================
            default -> today.plusDays(1);
        };
    }

    private static DayOfWeek toDayOfWeek(String week) {
        return switch (week) {
            case "MON" -> DayOfWeek.MONDAY;
            case "TUE" -> DayOfWeek.TUESDAY;
            case "WED" -> DayOfWeek.WEDNESDAY;
            case "THU" -> DayOfWeek.THURSDAY;
            case "FRI" -> DayOfWeek.FRIDAY;
            case "SAT" -> DayOfWeek.SATURDAY;
            case "SUN" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Invalid job_week: " + week);
        };
    }
}
