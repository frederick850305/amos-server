package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Component Type 聚合 DTO。保持前端字段名（typeNumber / classCode / parentTypeNumber /
 * compTypeModel / measurePointDefs / relatedTypes），避免直接暴露 JPA 实体。
 */
@Data
public class ComponentTypeDto {

    private Long id;
    private String typeNumber;
    private String name;
    private String maker;
    private String model;
    private String type;
    private String classCode;
    private String preferredVendor;
    private String parentTypeNumber;
    private String compTypeModel;
    private String description;
    private String status;
    private String dateCreated;
    private String dateModified;

    private List<ComponentTypeCounterDefDto> counters = new ArrayList<>();
    private List<ComponentTypeMeasurePointDefDto> measurePointDefs = new ArrayList<>();
    private List<ComponentTypeRelatedTypeDto> relatedTypes = new ArrayList<>();
    private List<ComponentTypeStockTypeDto> stockTypeLinks = new ArrayList<>();

    /** 派生字段：关联作业数量，由 Service 计算。 */
    private Integer jobs;
}
