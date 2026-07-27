package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 组件层级树节点（GET /hierarchy 返回）。递归 children 表示 parentComponent 父子关系。
 */
@Data
public class ComponentNodeDto {

    private Long id;
    private String number;
    private String name;
    private String status;
    private String typeNumber;
    private String functionNo;
    private String parentComponent;

    private List<ComponentNodeDto> children = new ArrayList<>();
}
