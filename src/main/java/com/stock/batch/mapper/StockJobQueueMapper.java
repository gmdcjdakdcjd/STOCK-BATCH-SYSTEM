package com.stock.batch.mapper;

import com.stock.batch.domain.StockJobQueue;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockJobQueueMapper {

    int insertWaiting(StockJobQueue waiting);

    StockJobQueue selectOneWaitingForUpdate();

    int updateStatusRunning(Long waitId);

    int deleteById(Long waitId);
}
