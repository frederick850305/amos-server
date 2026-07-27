package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件安装 / 拆卸历史（手册 Component Locations：Functions Performed）。
 */
@Entity
@Table(name = "component_function_history")
@Data
@NoArgsConstructor
public class ComponentFunctionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long componentId;
    private String componentNo;
    private String functionNo;
    private String functionDescription;
    private String location;
    private String action;
    private String performedBy;
    private String performedAt;
}
