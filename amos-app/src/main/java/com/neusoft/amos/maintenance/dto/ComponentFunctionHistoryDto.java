package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 组件安装/拆卸历史 DTO（对应前端 Functions Performed 标签）。
 */
@Data
public class ComponentFunctionHistoryDto {

    private Long id;
    private Long componentId;
    private String componentNo;
    private String functionNo;
    private String functionDescription;
    private String location;
    private String action;
    private String performedBy;
    private String performedAt;
}
