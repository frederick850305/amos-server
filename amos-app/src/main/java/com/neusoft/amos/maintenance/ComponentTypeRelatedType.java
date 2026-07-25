package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关联类型。一个 Component Type 可关联其它 Component Type（横向关系，区别于 V2 的父子层级）。
 */
@Entity
@Table(name = "component_type_related_type")
@Data
@NoArgsConstructor
public class ComponentTypeRelatedType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "component_type_id")
    @JsonIgnore
    private ComponentType componentType;

    @ManyToOne
    @JoinColumn(name = "related_component_type_id")
    private ComponentType relatedComponentType;
}
