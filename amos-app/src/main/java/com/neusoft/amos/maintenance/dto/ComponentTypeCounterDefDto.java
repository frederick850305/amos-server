package com.neusoft.amos.maintenance.dto;

import lombok.Data;

@Data
public class ComponentTypeCounterDefDto {
    private Long id;
    private String code;
    private String description;
    private String unit;
    private Integer sortOrder;
}
