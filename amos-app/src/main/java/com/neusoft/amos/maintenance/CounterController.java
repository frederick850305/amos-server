package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.CounterOverviewItemDto;
import com.neusoft.amos.maintenance.dto.CounterReadingLogDto;
import com.neusoft.amos.maintenance.dto.CounterReadingRequest;
import com.neusoft.amos.maintenance.dto.MeasurePointReadingLogDto;
import com.neusoft.amos.maintenance.dto.MeasurePointReadingRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Counters / Measure Points 命令与查询端点。
 */
@RestController
@RequestMapping("/api/maintenance")
public class CounterController {

    private final CounterService service;

    public CounterController(CounterService service) {
        this.service = service;
    }

    @GetMapping("/counters/overview")
    public List<CounterOverviewItemDto> overview(
            @RequestParam(required = false) String installation,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String component,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) Boolean inherits) {
        return service.getOverview(installation, department, component, function, inherits);
    }

    @PostMapping("/components/{componentId}/counters/{counterId}/readings")
    public Map<String, Object> recordComponentCounterReading(
            @PathVariable Long componentId,
            @PathVariable Long counterId,
            @RequestBody CounterReadingRequest request) {
        return service.recordComponentCounterReading(componentId, counterId, request);
    }

    @PostMapping("/functions/{functionId}/counters/{counterId}/readings")
    public Map<String, Object> recordFunctionCounterReading(
            @PathVariable Long functionId,
            @PathVariable Long counterId,
            @RequestBody CounterReadingRequest request) {
        return service.recordFunctionCounterReading(functionId, counterId, request);
    }

    @PostMapping("/components/{componentId}/measure-points/{pointId}/readings")
    public Map<String, Object> recordMeasurePointReading(
            @PathVariable Long componentId,
            @PathVariable Long pointId,
            @RequestBody MeasurePointReadingRequest request) {
        return service.recordMeasurePointReading(componentId, pointId, request);
    }

    @PostMapping("/components/{componentId}/counters/{counterId}/set-start")
    public Map<String, Object> setStart(
            @PathVariable Long componentId,
            @PathVariable Long counterId) {
        return Map.of("componentCounter", service.setStart(componentId, counterId));
    }

    @GetMapping("/counter-logs")
    public List<CounterReadingLogDto> counterLogs(
            @RequestParam(required = false) String component,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return service.getCounterLogs(component, function, from, to);
    }

    @GetMapping("/measure-logs")
    public List<MeasurePointReadingLogDto> measureLogs(
            @RequestParam(required = false) String component,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return service.getMeasureLogs(component, function, from, to);
    }
}
