package com.neusoft.amos.register;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册表：地点（手册 Chapter 3 Stock Item Locations）。
 * 安装地点作用域 + 自引用父级（支持层级）。被 components / stock_items / deliveries 引用。
 */
@Entity
@Table(name = "location_register",
        uniqueConstraints = @UniqueConstraint(columnNames = {"installation_id", "code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long installationId;
    private String code;
    private String name;
    private Long parentLocationId;
    private String locationType;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(length = 1000)
    private String remarks;
}
