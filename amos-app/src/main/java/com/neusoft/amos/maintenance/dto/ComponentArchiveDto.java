package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 组件档案 DTO（查询端点 GET /{id}/archive 返回）。
 */
@Data
public class ComponentArchiveDto {

    private Long id;
    private String componentNo;
    private String kind;
    private String fromDepartment;
    private String toDepartment;
    private String archiveDate;
    private String data;
}
