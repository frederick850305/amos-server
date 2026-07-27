package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件安装 / 拆卸轮次日志（手册 Component Locations：Rotation Log）。
 * 不可变：安装/拆卸时写入，支撑 Functions 窗口 History 与通用窗口 Rotation Log 标签。
 */
@Entity
@Table(name = "component_function_rotation")
@Data
@NoArgsConstructor
public class ComponentFunctionRotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "function_id")
    private Long functionId;
    @Column(name = "component_id")
    private Long componentId;
    @Column(name = "component_no")
    private String componentNo;
    @Column(name = "component_name")
    private String componentName;
    private String action;
    @Column(name = "performed_by")
    private String performedBy;
    @Column(name = "performed_at")
    private String performedAt;
    private String details;
    @Column(name = "new_location")
    private String newLocation;
    @Column(name = "new_status")
    private String newStatus;
    @Column(name = "installed_at")
    private String installedAt;
    @Column(name = "removed_at")
    private String removedAt;
}
