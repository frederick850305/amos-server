package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计数器读数日志 DTO。
 */
@Data
public class CounterReadingLogDto {

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
