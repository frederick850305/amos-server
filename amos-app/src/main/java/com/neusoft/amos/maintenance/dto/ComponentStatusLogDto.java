package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 组件状态变更日志 DTO（查询端点 GET /{id}/status-log 返回）。
 */
@Data
public class ComponentStatusLogDto {

    private Long id;
    private Long componentId;
    private String componentNo;
    private String componentName;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedBy;
    private String changedAt;
}
