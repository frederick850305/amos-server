package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 测点读数更新请求。
 */
@Data
public class MeasurePointReadingRequest {

    private String value;
    private String trend;
    private String readingDate;
}
