package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 组件计数器 DTO（对应前端 Counters 子表）。
 */
@Data
public class ComponentCounterDto {

    private Long id;
    private String code;
    private String description;
    private String unit;
    private Double currentValue;
    private String dependsOn;

    private String latestZeroedDate;
    private BigDecimal startValue;
    private BigDecimal average;
    private String calculate;
}
