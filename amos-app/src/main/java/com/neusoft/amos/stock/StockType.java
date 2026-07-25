package com.neusoft.amos.stock;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 备件主数据。对应 mock 的 stockTypes；maker 为短编码，引用 maker_register。
 */
@Entity
@Table(name = "stock_type")
@Data
@NoArgsConstructor
public class StockType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String stockTypeNo;

    private String description;
    private String maker;
    private String makersRef;
    private String vendor;
    private String grade;
    private String unit;
    private Double bestPrice;
    private String status;
    private String installation;

    @OneToMany(mappedBy = "stockType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTypeMaker> makers = new ArrayList<>();

    @OneToMany(mappedBy = "stockType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTypeVendor> vendors = new ArrayList<>();

    @OneToMany(mappedBy = "stockType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockGrade> grades = new ArrayList<>();
}
