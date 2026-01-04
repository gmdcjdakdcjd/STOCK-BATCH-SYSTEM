package com.stock.batch.mapper;

import com.stock.batch.domain.BatchInHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BatchInHistoryMapper {
    /**
     * BatchIn 실행 이력 저장
     */
    int insertBatchInHistory(BatchInHistory history);
}
