package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：作业等级（Job Class）。被 jobs.class_code 引用。
 */
@Entity
@Table(name = "job_class")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobClass {

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
