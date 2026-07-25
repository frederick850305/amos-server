package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 组件类型模板（Fleet 级共享）。对应 mock 的 componentTypes。
 * 聚合根：持有 counters / measurePointDefs / relatedTypes / stockTypeLinks 子集合。
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

    @Column(name = "comp_type_model", length = 100)
    private String compTypeModel;

    @Column(length = 1000)
    private String description;

    private String status;
    private String dateCreated;
    private String dateModified;

    @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentTypeCounterDef> counters = new ArrayList<>();

    @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentTypeMeasurePointDef> measurePointDefs = new ArrayList<>();

    @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentTypeRelatedType> relatedTypes = new ArrayList<>();

    @OneToMany(mappedBy = "componentType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentTypeStockType> stockTypeLinks = new ArrayList<>();
}
