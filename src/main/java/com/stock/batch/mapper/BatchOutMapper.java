package com.stock.batch.mapper;

import com.stock.batch.domain.BatchOut;
import com.stock.batch.domain.BatchOutHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchOutMapper {
    /**
     * 현재 시각 기준 실행 가능한 BatchOut 조회
     */
    List<BatchOut> selectExecutableBatchOut(
            @Param("todayMonth") String todayMonth,
            @Param("todayDay")   String todayDay,
            @Param("todayWeek")  String todayWeek,
            @Param("nowHour")    String nowHour,
            @Param("nowMin")     String nowMin
    );

    int updateExecutionStatus(BatchOut job);
    int resetAllActGb();

    int resetActGbByLastExecBeforeToday();

}
