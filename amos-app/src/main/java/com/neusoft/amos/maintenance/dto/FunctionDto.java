package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能位置聚合 DTO。字段名对齐前端（functionNo / parentFunctionNo / installedComponentId 等），
 * 不直接暴露 JPA 实体。子集合：functionCounters / rotationLog。
 */
@Data
public class FunctionDto {

    private Long id;
    private String functionNo;
    private String installation;
    private String department;
    private String description;
    private String reference;
    private String parentFunctionNo;
    private String status;
    private String location;
    private String criticality;
    // 安装组件的编号（业务编码字符串），由 installed_component_no 回填，供前端 lookup
    private String installedComponentId;

    private String sfiCode;
    private String system;
    private String subSystem;
    private String remarks;
    private String serialNo;
    private String maker;
    private String model;
    private String tagNo;
    private BigDecimal assetValue;
    private String acquisitionDate;
    private String currency;
    private BigDecimal depreciation;

    private List<FunctionCounterDto> functionCounters = new ArrayList<>();
    private List<FunctionRotationDto> rotationLog = new ArrayList<>();
}
