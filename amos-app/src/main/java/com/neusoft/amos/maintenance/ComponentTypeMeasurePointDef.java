package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类型级测点定义。对应手册 P34-35：定义 Component Type 下可挂的监测点模板。
 */
@Entity
@Table(name = "component_type_measure_point_def")
@Data
@NoArgsConstructor
public class ComponentTypeMeasurePointDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "component_type_id")
    @JsonIgnore
    private ComponentType componentType;

    private String code;
    private String description;
    private String trend;
    private String unit;
    private Integer sortOrder;
}
