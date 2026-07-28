package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测点读数日志 DTO。
 */
@Data
public class MeasurePointReadingLogDto {

    private Long id;
    private Long componentMeasurePointId;
    private String componentNo;
    private String functionNo;
    private String code;
    private String description;
    private String value;
    private String trend;
    private String readingDate;
    private LocalDateTime createdAt;
    private String createdBy;
}
