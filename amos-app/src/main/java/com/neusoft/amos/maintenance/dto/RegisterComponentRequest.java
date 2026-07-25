package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * Register as Component 命令入参（对应手册 P30：将 Component Type 注册为实际 Component）。
 */
@Data
public class RegisterComponentRequest {
    private String number;
    private String name;
    private String location;
    private String department;
    private String installation;
    private String serialNo;
}
