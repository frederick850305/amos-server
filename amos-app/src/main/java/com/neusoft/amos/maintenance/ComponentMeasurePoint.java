package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件测点（手册 P35 Component Measure Points）。对应前端 Measure Points 子表。
 */
@Entity
@Table(name = "component_measure_point")
@Data
@NoArgsConstructor
public class ComponentMeasurePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "component_id")
    @JsonIgnore
    private Component component;

    private String code;
    private String description;
    private String unit;
    private String trend;
    @Column(name = "reading")
    private String value;
    private String lastReadDate;
}
