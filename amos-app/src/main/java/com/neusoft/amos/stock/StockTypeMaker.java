package com.neusoft.amos.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合格制造商子表（手册 P181）。对应 mock 的 stockTypeMakers。
 */
@Entity
@Table(name = "stock_type_maker")
@Data
@NoArgsConstructor
public class StockTypeMaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockTypeNo;
    private String maker;
    private String makersRef;
    private Double price;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_type_id")
    @JsonIgnore
    private StockType stockType;
}
