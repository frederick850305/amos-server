package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 组件/功能计数器不可变读数日志。
 * component_counter_id 仅作引用（无 FK，避免删计数器时连带删历史）。
 */
@Entity
@Table(name = "counter_reading_log")
@Data
@NoArgsConstructor
public class CounterReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long componentCounterId;
    private String componentNo;
    private String functionNo;
    private String code;
    private String description;
    private BigDecimal oldValue;
    private BigDecimal newValue;
    private BigDecimal delta;
    private String readingDate;
    private LocalDateTime createdAt;
    private String createdBy;
}
