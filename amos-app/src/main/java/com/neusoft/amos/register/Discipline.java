package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：专业（Discipline）。被 jobs 的 required_discipline 引用。
 */
@Entity
@Table(name = "discipline")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String name;
    private String description;

    @Column(nullable = false)
    private String status = "ACTIVE";
}
