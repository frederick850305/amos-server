package com.neusoft.amos.maintenance.dto;

import lombok.Data;

@Data
public class ComponentTypeMeasurePointDefDto {
    private Long id;
    private String code;
    private String description;
    private String trend;
    private String unit;
    private Integer sortOrder;
}
