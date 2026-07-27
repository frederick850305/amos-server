package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 从功能位置拆卸组件请求（手册 Removing a Component from a Function）。
 */
@Data
public class RemoveComponentRequest {
    private String newLocation;
    private String status;
    private String details;
    private Boolean cascadeSubFunctions = false;
}
