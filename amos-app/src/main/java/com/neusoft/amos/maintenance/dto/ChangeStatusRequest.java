package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 状态变更请求。被 Component 与 Function 共用：
 *  - Component 使用 changedBy / reason / cascadeSubComponents；
 *  - Function 使用 cascadeSubFunctions。
 */
@Data
public class ChangeStatusRequest {
    private String newStatus;
    private String changedBy;
    private String reason;
    private boolean cascadeSubComponents = false;
    private Boolean cascadeSubFunctions = false;
}
