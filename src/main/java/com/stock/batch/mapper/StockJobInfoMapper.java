package com.stock.batch.mapper;

import com.stock.batch.domain.StockJobInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockJobInfoMapper {

    StockJobInfo selectByJobCode(String jobCode);

}
