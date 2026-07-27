package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件状态变更日志（不可变）。每次 change-status 命令追加一条。
 */
@Entity
@Table(name = "component_status_log")
@Data
@NoArgsConstructor
public class ComponentStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long componentId;
    private String componentNo;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedBy;
    private String changedAt;
}
