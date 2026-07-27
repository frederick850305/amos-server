package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Component 聚合 DTO。保持前端字段名（typeNumber / functionNo / parentComponent 等），
 * 不直接暴露 JPA 实体。子集合：componentCounters / componentMeasurePoints。
 */
@Data
public class ComponentDto {

    private Long id;
    private String number;
    private String typeNumber;
    private String name;
    private String status;
    private String maker;
    private String type;
    private String serialNo;
    private String location;
    private String department;
    private String vendor;
    private String functionNo;
    private String installDate;
    private String installation;

    private String parentComponent;
    private String componentTypeModel;
    private String dateCreated;
    private String dateModified;

    private List<ComponentCounterDto> componentCounters = new ArrayList<>();
    private List<ComponentMeasurePointDto> componentMeasurePoints = new ArrayList<>();
}
