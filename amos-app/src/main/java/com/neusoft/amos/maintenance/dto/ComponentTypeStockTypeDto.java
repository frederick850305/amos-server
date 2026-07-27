package com.neusoft.amos.maintenance.dto;

import lombok.Data;

@Data
public class ComponentTypeStockTypeDto {
    private Long id;
    private Long stockTypeId;
    private String stockTypeNo;
    private String description;
    private Double quantity;
    private String makersRef;
    private String remarks;
    private String alternativeNo;
}
