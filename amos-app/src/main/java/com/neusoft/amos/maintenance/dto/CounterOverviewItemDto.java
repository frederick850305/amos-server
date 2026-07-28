package com.neusoft.amos.maintenance.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Counter Overview 聚合项：一个组件及其计数器，以及所装功能位置的计数器。
 */
@Data
public class CounterOverviewItemDto {

    private Long componentId;
    private String componentNo;
    private String componentName;
    private String installation;
    private String department;
    private String functionNo;

    private List<ComponentCounterDto> counters = new ArrayList<>();
    private List<FunctionCounterDto> functionCounters = new ArrayList<>();
}
