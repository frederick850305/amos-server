package com.neusoft.amos.maintenance.dto;

import lombok.Data;

@Data
public class ComponentTypeRelatedTypeDto {
    private Long id;
    private Long relatedComponentTypeId;
    private String relatedTypeNumber;
    private String relatedTypeName;
}
