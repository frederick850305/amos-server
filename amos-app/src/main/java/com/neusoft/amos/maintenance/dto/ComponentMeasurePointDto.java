package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 组件测点 DTO（对应前端 Measure Points 子表）。
 */
@Data
public class ComponentMeasurePointDto {

    private Long id;
    private String code;
    private String description;
    private String unit;
    private String trend;
    private String value;
    private String lastReadDate;
}
