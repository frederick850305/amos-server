package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件计数器。dependsOn 指向另一组件（如活塞依赖主机 C-10001）实现读数级联。
 */
@Entity
@Table(name = "component_counter")
@Data
@NoArgsConstructor
public class ComponentCounter {

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
    private Double currentValue;
    private String dependsOn;
}
