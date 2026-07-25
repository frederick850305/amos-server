package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：功能关键性（手册 Chapter 2 Function Criticality, printed page 44）。
 * 被 functions.criticality_id 引用。用 active 布尔表示启用状态。
 */
@Entity
@Table(name = "function_criticality")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCriticality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String degree;

    private String description;
    private String color;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;
}
