package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * change-status 命令请求体。
 */
@Data
public class ChangeStatusRequest {
    private String newStatus;
    private boolean cascadeSubComponents = false;
    private String reason;
    private String changedBy;
}
