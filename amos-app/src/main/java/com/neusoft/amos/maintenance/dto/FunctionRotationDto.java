package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 组件安装 / 拆卸轮次 DTO（对应前端 Rotation Log）。
 */
@Data
public class FunctionRotationDto {

    private Long id;
    private String componentNo;
    private String componentName;
    private String action;
    private String performedBy;
    private String performedAt;
    private String details;
    private String newLocation;
    private String newStatus;
    private String installedAt;
    private String removedAt;
}
