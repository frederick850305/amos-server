package com.neusoft.amos.maintenance;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 组件测点不可变读数日志。
 * component_measure_point_id 仅作引用（无 FK）。
 */
@Entity
@Table(name = "measure_point_reading_log")
@Data
@NoArgsConstructor
public class MeasurePointReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long componentMeasurePointId;
    private String componentNo;
    private String functionNo;
    private String code;
    private String description;
    @Column(name = "reading_value")
    private String value;
    private String trend;
    private String readingDate;
    private LocalDateTime createdAt;
    private String createdBy;
}
