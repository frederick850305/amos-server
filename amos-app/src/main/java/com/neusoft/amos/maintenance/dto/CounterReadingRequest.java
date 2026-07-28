package com.neusoft.amos.maintenance.dto;

import lombok.Data;

/**
 * 计数器读数更新请求。
 */
@Data
public class CounterReadingRequest {

    private Double newValue;
    private String readingDate;
    private String unit;
}
