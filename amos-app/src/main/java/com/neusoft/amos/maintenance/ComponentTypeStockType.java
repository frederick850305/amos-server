package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neusoft.amos.stock.StockType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关联备件类型（Parts / Stock Types）。一个 Component Type 可关联其维修所需备件类型及其数量。
 */
@Entity
@Table(name = "component_type_stock_type")
@Data
@NoArgsConstructor
public class ComponentTypeStockType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "component_type_id")
    @JsonIgnore
    private ComponentType componentType;

    @ManyToOne
    @JoinColumn(name = "stock_type_id")
    private StockType stockType;

    private String makersRef;
    private Double quantity;
    private String remarks;

    @Column(name = "alternative_no")
    private String alternativeNo;
}
