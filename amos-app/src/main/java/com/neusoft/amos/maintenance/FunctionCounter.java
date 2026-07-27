package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 功能位置计数器（手册 Function Counters）。对应前端 Counters 子表。
 */
@Entity
@Table(name = "function_counter")
@Data
@NoArgsConstructor
public class FunctionCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "function_id")
    @JsonIgnore
    private MaintenanceFunction function;

    private String code;
    private String description;
    private String unit;
    @Column(name = "last_value")
    private BigDecimal lastValue;
}
