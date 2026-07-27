package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 功能位置计数器 DTO（对应前端 Counters 子表）。
 */
@Data
public class FunctionCounterDto {

    private Long id;
    private String code;
    private String description;
    private String unit;
    private BigDecimal lastValue;
}
