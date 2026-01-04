package com.stock.batch.mapper;

import com.stock.batch.domain.BatchIn;
import com.stock.batch.domain.BatchInHistory;
import com.stock.batch.domain.BatchOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchInMapper {
    /**
     * 현재 시각 기준 실행 가능한 BatchIn 조회
     */
    List<BatchIn> selectExecutableBatchIn(
            @Param("todayMonth") String todayMonth,
            @Param("todayDay")   String todayDay,
            @Param("todayWeek")  String todayWeek,
            @Param("nowHour")    String nowHour,
            @Param("nowMin")     String nowMin
    );

    int resetAllActGb();

    int updateExecutionStatus(BatchIn job);

    int resetActGbByLastExecBeforeToday();
}