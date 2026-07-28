package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.ComponentCounterDto;
import com.neusoft.amos.maintenance.dto.ComponentMeasurePointDto;
import com.neusoft.amos.maintenance.dto.CounterOverviewItemDto;
import com.neusoft.amos.maintenance.dto.CounterReadingLogDto;
import com.neusoft.amos.maintenance.dto.CounterReadingRequest;
import com.neusoft.amos.maintenance.dto.FunctionCounterDto;
import com.neusoft.amos.maintenance.dto.MeasurePointReadingLogDto;
import com.neusoft.amos.maintenance.dto.MeasurePointReadingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Counters / Measure Points 聚合服务：
 * - 组件/功能计数器读数更新命令（写不可变日志、更新当前值、重算平均、级联依赖组件、同步功能计数器）
 * - 组件测点读数更新命令（写日志、更新值/趋势）
 * - 组件计数器归零（Set Start）
 * - Counter Overview 聚合查询
 * - counter / measure 读数日志查询
 */
@Service
@RequiredArgsConstructor
public class CounterService {

    private final ComponentRepository componentRepository;
    private final FunctionRepository functionRepository;
    private final ComponentCounterRepository counterRepository;
    private final FunctionCounterRepository functionCounterRepository;
    private final ComponentMeasurePointRepository measurePointRepository;
    private final CounterReadingLogRepository counterLogRepository;
    private final MeasurePointReadingLogRepository measureLogRepository;

    private static final String PERFORMED_BY = "A. Admin";

    // ---- 组件计数器读数更新 ----
    @Transactional
    public Map<String, Object> recordComponentCounterReading(Long componentId, Long counterId,
                                                              CounterReadingRequest req) {
        Component comp = componentRepository.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("component not found: " + componentId));
        ComponentCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("counter not found: " + counterId));
        if (counter.getComponent() == null || !counter.getComponent().getId().equals(componentId)) {
            throw new IllegalArgumentException("counter does not belong to component");
        }
        if (req.getNewValue() == null) {
            throw new IllegalArgumentException("newValue is required");
        }

        BigDecimal oldVal = toBigDecimal(counter.getCurrentValue());
        BigDecimal newVal = BigDecimal.valueOf(req.getNewValue());
        BigDecimal delta = newVal.subtract(oldVal);
        String readingDate = (req.getReadingDate() != null && !req.getReadingDate().isBlank())
                ? req.getReadingDate() : LocalDate.now().toString();

        counter.setCurrentValue(req.getNewValue());
        counter.setLatestZeroedDate(readingDate);
        counter.setAverage(computeAverage(req.getNewValue(), comp.getInstallDate(), readingDate));
        counterRepository.save(counter);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("componentCounter", toCounterDto(counter));
        List<ComponentCounterDto> dependentUpdates = new ArrayList<>();
        result.put("dependentUpdates", dependentUpdates);
        result.put("functionCounter", null);
        result.put("log", writeComponentCounterLog(counter, comp.getNumber(), comp.getFunctionNo(),
                oldVal, newVal, delta, readingDate));

        // 级联：其它组件里 dependsOn==本组件编号 且 code 相同的计数器同步新值
        if (counter.getCode() != null) {
            for (ComponentCounter dep : counterRepository.findAll()) {
                if (dep.getId().equals(counterId)) continue;
                if (counter.getCode().equals(dep.getCode())
                        && comp.getNumber() != null && comp.getNumber().equals(dep.getDependsOn())) {
                    BigDecimal dOld = toBigDecimal(dep.getCurrentValue());
                    Component depComp = dep.getComponent();
                    dep.setCurrentValue(req.getNewValue());
                    dep.setLatestZeroedDate(readingDate);
                    dep.setAverage(computeAverage(req.getNewValue(),
                            depComp != null ? depComp.getInstallDate() : null, readingDate));
                    counterRepository.save(dep);
                    writeComponentCounterLog(dep,
                            depComp != null ? depComp.getNumber() : null,
                            depComp != null ? depComp.getFunctionNo() : null,
                            dOld, newVal, delta, readingDate);
                    syncFunctionCounter(depComp, dep.getDescription(), delta);
                    dependentUpdates.add(toCounterDto(dep));
                }
            }
            result.put("dependentUpdates", dependentUpdates);
        }

        // 同步功能计数器：组件装在功能位置且其计数器 description 相同
        FunctionCounterDto synced = syncFunctionCounter(comp, counter.getDescription(), delta);
        if (synced != null) result.put("functionCounter", synced);
        return result;
    }

    // ---- 功能计数器读数更新（终点，不反向同步组件）----
    @Transactional
    public Map<String, Object> recordFunctionCounterReading(Long functionId, Long counterId,
                                                             CounterReadingRequest req) {
        MaintenanceFunction fn = functionRepository.findById(functionId)
                .orElseThrow(() -> new IllegalArgumentException("function not found: " + functionId));
        FunctionCounter fc = functionCounterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("function counter not found: " + counterId));
        if (fc.getFunction() == null || !fc.getFunction().getId().equals(functionId)) {
            throw new IllegalArgumentException("function counter does not belong to function");
        }
        if (req.getNewValue() == null) {
            throw new IllegalArgumentException("newValue is required");
        }
        BigDecimal oldVal = fc.getLastValue() != null ? fc.getLastValue() : BigDecimal.ZERO;
        BigDecimal newVal = BigDecimal.valueOf(req.getNewValue());
        BigDecimal delta = newVal.subtract(oldVal);
        String readingDate = (req.getReadingDate() != null && !req.getReadingDate().isBlank())
                ? req.getReadingDate() : LocalDate.now().toString();

        fc.setLastValue(newVal);
        functionCounterRepository.save(fc);

        CounterReadingLog log = new CounterReadingLog();
        log.setComponentCounterId(null);
        log.setComponentNo(null);
        log.setFunctionNo(fn.getFunctionNo());
        log.setCode(fc.getCode());
        log.setDescription(fc.getDescription());
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setDelta(delta);
        log.setReadingDate(readingDate);
        log.setCreatedAt(LocalDateTime.now());
        log.setCreatedBy(PERFORMED_BY);
        counterLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("functionCounter", toFunctionCounterDto(fc));
        result.put("log", toCounterLogDto(log));
        return result;
    }

    // ---- 组件测点读数更新 ----
    @Transactional
    public Map<String, Object> recordMeasurePointReading(Long componentId, Long pointId,
                                                          MeasurePointReadingRequest req) {
        Component comp = componentRepository.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("component not found: " + componentId));
        ComponentMeasurePoint mp = measurePointRepository.findById(pointId)
                .orElseThrow(() -> new IllegalArgumentException("measure point not found: " + pointId));
        if (mp.getComponent() == null || !mp.getComponent().getId().equals(componentId)) {
            throw new IllegalArgumentException("measure point does not belong to component");
        }
        String readingDate = (req.getReadingDate() != null && !req.getReadingDate().isBlank())
                ? req.getReadingDate() : LocalDate.now().toString();

        mp.setValue(req.getValue());
        mp.setTrend(req.getTrend());
        mp.setLastReadDate(readingDate);
        measurePointRepository.save(mp);

        MeasurePointReadingLog log = new MeasurePointReadingLog();
        log.setComponentMeasurePointId(mp.getId());
        log.setComponentNo(comp.getNumber());
        log.setFunctionNo(comp.getFunctionNo());
        log.setCode(mp.getCode());
        log.setDescription(mp.getDescription());
        log.setValue(req.getValue());
        log.setTrend(req.getTrend());
        log.setReadingDate(readingDate);
        log.setCreatedAt(LocalDateTime.now());
        log.setCreatedBy(PERFORMED_BY);
        measureLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("measurePoint", toMeasurePointDto(mp));
        result.put("log", toMeasureLogDto(log));
        return result;
    }

    // ---- 组件计数器归零（Set Start：快照当前值为 startValue，重置平均基准）----
    @Transactional
    public ComponentCounterDto setStart(Long componentId, Long counterId) {
        componentRepository.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("component not found: " + componentId));
        ComponentCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("counter not found: " + counterId));
        if (counter.getComponent() == null || !counter.getComponent().getId().equals(componentId)) {
            throw new IllegalArgumentException("counter does not belong to component");
        }
        counter.setStartValue(BigDecimal.valueOf(counter.getCurrentValue() != null ? counter.getCurrentValue() : 0));
        counter.setLatestZeroedDate(LocalDate.now().toString());
        counter.setAverage(BigDecimal.ZERO);
        counterRepository.save(counter);
        return toCounterDto(counter);
    }

    // ---- Counter Overview 聚合 ----
    public List<CounterOverviewItemDto> getOverview(String installation, String department,
                                                    String component, String function, Boolean inherits) {
        List<Component> comps = componentRepository.findAll();
        List<CounterOverviewItemDto> items = new ArrayList<>();
        for (Component c : comps) {
            if (installation != null && !installation.equals(c.getInstallation())) continue;
            if (department != null && !department.equals(c.getDepartment())) continue;
            if (component != null && (c.getNumber() == null || !c.getNumber().contains(component))) continue;
            if (function != null && (c.getFunctionNo() == null || !c.getFunctionNo().contains(function))) continue;

            CounterOverviewItemDto item = new CounterOverviewItemDto();
            item.setComponentId(c.getId());
            item.setComponentNo(c.getNumber());
            item.setComponentName(c.getName());
            item.setInstallation(c.getInstallation());
            item.setDepartment(c.getDepartment());
            item.setFunctionNo(c.getFunctionNo());

            List<ComponentCounterDto> cds = c.getComponentCounters().stream()
                    .map(this::toCounterDto).collect(Collectors.toList());
            if (Boolean.TRUE.equals(inherits)) {
                cds = cds.stream()
                        .filter(x -> x.getDependsOn() != null && !x.getDependsOn().isBlank())
                        .collect(Collectors.toList());
            }
            item.setCounters(cds);

            if (c.getFunctionNo() != null && !c.getFunctionNo().isBlank()) {
                MaintenanceFunction fn = functionRepository
                        .findByFunctionNoAndInstallation(c.getFunctionNo(), c.getInstallation())
                        .orElseGet(() -> functionRepository.findByFunctionNo(c.getFunctionNo()).orElse(null));
                if (fn != null) {
                    item.setFunctionCounters(fn.getFunctionCounters().stream()
                            .map(this::toFunctionCounterDto).collect(Collectors.toList()));
                }
            }
            items.add(item);
        }
        return items;
    }

    // ---- 计数器读数日志查询 ----
    public List<CounterReadingLogDto> getCounterLogs(String component, String function,
                                                     String from, String to) {
        List<CounterReadingLog> logs;
        if (component != null) logs = counterLogRepository.findByComponentNoContainingIgnoreCase(component);
        else if (function != null) logs = counterLogRepository.findByFunctionNoContainingIgnoreCase(function);
        else logs = counterLogRepository.findAll();
        logs = filterCounterLogsByDate(logs, from, to);
        return logs.stream().map(this::toCounterLogDto).collect(Collectors.toList());
    }

    // ---- 测点读数日志查询 ----
    public List<MeasurePointReadingLogDto> getMeasureLogs(String component, String function,
                                                           String from, String to) {
        List<MeasurePointReadingLog> logs;
        if (component != null) logs = measureLogRepository.findByComponentNoContainingIgnoreCase(component);
        else if (function != null) logs = measureLogRepository.findByFunctionNoContainingIgnoreCase(function);
        else logs = measureLogRepository.findAll();
        logs = filterMeasureLogsByDate(logs, from, to);
        return logs.stream().map(this::toMeasureLogDto).collect(Collectors.toList());
    }

    // ---- helpers ----
    private List<CounterReadingLog> filterCounterLogsByDate(List<CounterReadingLog> logs, String from, String to) {
        List<CounterReadingLog> out = new ArrayList<>();
        for (CounterReadingLog l : logs) {
            if (from != null && l.getReadingDate() != null && l.getReadingDate().compareTo(from) < 0) continue;
            if (to != null && l.getReadingDate() != null && l.getReadingDate().compareTo(to) > 0) continue;
            out.add(l);
        }
        return out;
    }

    private List<MeasurePointReadingLog> filterMeasureLogsByDate(List<MeasurePointReadingLog> logs, String from, String to) {
        List<MeasurePointReadingLog> out = new ArrayList<>();
        for (MeasurePointReadingLog l : logs) {
            if (from != null && l.getReadingDate() != null && l.getReadingDate().compareTo(from) < 0) continue;
            if (to != null && l.getReadingDate() != null && l.getReadingDate().compareTo(to) > 0) continue;
            out.add(l);
        }
        return out;
    }

    private FunctionCounterDto syncFunctionCounter(Component comp, String description, BigDecimal delta) {
        if (comp == null || comp.getFunctionNo() == null || comp.getFunctionNo().isBlank()) return null;
        if (description == null) return null;
        MaintenanceFunction fn = functionRepository
                .findByFunctionNoAndInstallation(comp.getFunctionNo(), comp.getInstallation())
                .orElseGet(() -> functionRepository.findByFunctionNo(comp.getFunctionNo()).orElse(null));
        if (fn == null) return null;
        for (FunctionCounter fc : fn.getFunctionCounters()) {
            if (description.equals(fc.getDescription())) {
                BigDecimal fOld = fc.getLastValue() != null ? fc.getLastValue() : BigDecimal.ZERO;
                fc.setLastValue(fOld.add(delta));
                functionRepository.save(fn);
                return toFunctionCounterDto(fc);
            }
        }
        return null;
    }

    private BigDecimal toBigDecimal(Double v) {
        return v != null ? BigDecimal.valueOf(v) : BigDecimal.ZERO;
    }

    private BigDecimal computeAverage(Double currentValue, String baselineDate, String readingDate) {
        if (currentValue == null) return BigDecimal.ZERO;
        if (baselineDate == null) return BigDecimal.valueOf(currentValue);
        try {
            LocalDate b = LocalDate.parse(baselineDate);
            LocalDate r = LocalDate.parse(readingDate);
            long days = java.time.temporal.ChronoUnit.DAYS.between(b, r);
            if (days <= 0) return BigDecimal.valueOf(currentValue);
            return BigDecimal.valueOf(currentValue)
                    .divide(BigDecimal.valueOf(days), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.valueOf(currentValue);
        }
    }

    private CounterReadingLog writeComponentCounterLog(ComponentCounter counter, String componentNo,
                                                        String functionNo, BigDecimal oldVal, BigDecimal newVal,
                                                        BigDecimal delta, String readingDate) {
        CounterReadingLog log = new CounterReadingLog();
        log.setComponentCounterId(counter.getId());
        log.setComponentNo(componentNo);
        log.setFunctionNo(functionNo);
        log.setCode(counter.getCode());
        log.setDescription(counter.getDescription());
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setDelta(delta);
        log.setReadingDate(readingDate);
        log.setCreatedAt(LocalDateTime.now());
        log.setCreatedBy(PERFORMED_BY);
        counterLogRepository.save(log);
        return log;
    }

    private ComponentCounterDto toCounterDto(ComponentCounter c) {
        ComponentCounterDto d = new ComponentCounterDto();
        d.setId(c.getId());
        d.setCode(c.getCode());
        d.setDescription(c.getDescription());
        d.setUnit(c.getUnit());
        d.setCurrentValue(c.getCurrentValue());
        d.setDependsOn(c.getDependsOn());
        d.setLatestZeroedDate(c.getLatestZeroedDate());
        d.setStartValue(c.getStartValue());
        d.setAverage(c.getAverage());
        d.setCalculate(c.getCalculate());
        return d;
    }

    private FunctionCounterDto toFunctionCounterDto(FunctionCounter c) {
        FunctionCounterDto d = new FunctionCounterDto();
        d.setId(c.getId());
        d.setCode(c.getCode());
        d.setDescription(c.getDescription());
        d.setUnit(c.getUnit());
        d.setLastValue(c.getLastValue());
        return d;
    }

    private ComponentMeasurePointDto toMeasurePointDto(ComponentMeasurePoint m) {
        ComponentMeasurePointDto d = new ComponentMeasurePointDto();
        d.setId(m.getId());
        d.setCode(m.getCode());
        d.setDescription(m.getDescription());
        d.setUnit(m.getUnit());
        d.setTrend(m.getTrend());
        d.setValue(m.getValue());
        d.setLastReadDate(m.getLastReadDate());
        return d;
    }

    private CounterReadingLogDto toCounterLogDto(CounterReadingLog l) {
        CounterReadingLogDto d = new CounterReadingLogDto();
        d.setId(l.getId());
        d.setComponentCounterId(l.getComponentCounterId());
        d.setComponentNo(l.getComponentNo());
        d.setFunctionNo(l.getFunctionNo());
        d.setCode(l.getCode());
        d.setDescription(l.getDescription());
        d.setOldValue(l.getOldValue());
        d.setNewValue(l.getNewValue());
        d.setDelta(l.getDelta());
        d.setReadingDate(l.getReadingDate());
        d.setCreatedAt(l.getCreatedAt());
        d.setCreatedBy(l.getCreatedBy());
        return d;
    }

    private MeasurePointReadingLogDto toMeasureLogDto(MeasurePointReadingLog l) {
        MeasurePointReadingLogDto d = new MeasurePointReadingLogDto();
        d.setId(l.getId());
        d.setComponentMeasurePointId(l.getComponentMeasurePointId());
        d.setComponentNo(l.getComponentNo());
        d.setFunctionNo(l.getFunctionNo());
        d.setCode(l.getCode());
        d.setDescription(l.getDescription());
        d.setValue(l.getValue());
        d.setTrend(l.getTrend());
        d.setReadingDate(l.getReadingDate());
        d.setCreatedAt(l.getCreatedAt());
        d.setCreatedBy(l.getCreatedBy());
        return d;
    }
}
