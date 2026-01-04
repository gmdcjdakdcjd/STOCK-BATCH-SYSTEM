package com.stock.batch.mapper;

import com.stock.batch.domain.BatchOutHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BatchOutHistoryMapper {

    /**
     * BatchOut 실행 이력 저장
     */
    int insertBatchOutHistory(BatchOutHistory history);
}
