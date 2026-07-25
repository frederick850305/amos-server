package com.neusoft.amos.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存等级子表（手册 P181）。对应 mock 的 stockGrades。
 */
@Entity
@Table(name = "stock_grade")
@Data
@NoArgsConstructor
public class StockGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockTypeNo;
    private String grade;
    private Double priceFactor;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_type_id")
    @JsonIgnore
    private StockType stockType;
}
