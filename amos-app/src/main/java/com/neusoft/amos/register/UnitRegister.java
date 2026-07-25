package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：计量单位（手册 Chapter 4 / Stock）。被 stock_types / stock_items 的 unit 引用。
 */
@Entity
@Table(name = "unit_register")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitRegister {

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
