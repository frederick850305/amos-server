package com.neusoft.amos.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合格供应商子表（手册 P181）。对应 mock 的 stockTypeVendors。
 */
@Entity
@Table(name = "stock_type_vendor")
@Data
@NoArgsConstructor
public class StockTypeVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockTypeNo;
    private String vendor;
    private Double price;
    private Integer leadTime;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_type_id")
    @JsonIgnore
    private StockType stockType;
}
