package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件类型模板（Fleet 级共享）。对应 mock 的 componentTypes。
 * 注：counters / measurePointDefs / parts 子表在 Phase 2 规范化，Phase 1 先保留核心字段。
 */
@Entity
@Table(name = "component_type")
@Data
@NoArgsConstructor
public class ComponentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String typeNumber;

    private String name;
    private String maker;
    private String model;
    private String type;
    private String classCode;
    private String preferredVendor;
    private String parentTypeNumber;

    @Column(length = 1000)
    private String description;

    private String status;
    private String dateCreated;
    private String dateModified;
}
