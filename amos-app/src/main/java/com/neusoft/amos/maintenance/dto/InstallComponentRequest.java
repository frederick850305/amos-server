package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 安装组件到功能位置请求（手册 Component Locations）。
 */
@Data
public class InstallComponentRequest {
    private String componentNumber;
    private String details;
}
