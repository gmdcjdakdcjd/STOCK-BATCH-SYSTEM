package com.stock.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonJobResult {
    private String status;    // SUCCESS or FAIL
    private int rowCount;     // 처리된 행 개수
    private int codeCount;    // 처리된 종목 개수
    private String errorMsg;  // 에러 메시지 (없으면 NO_ERROR)
    private Long resultId;    // strategy_result.id (추가)
}

