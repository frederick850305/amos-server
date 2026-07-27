package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.ChangeStatusRequest;
import com.neusoft.amos.maintenance.dto.ComponentArchiveDto;
import com.neusoft.amos.maintenance.dto.ComponentCounterDto;
import com.neusoft.amos.maintenance.dto.ComponentDto;
import com.neusoft.amos.maintenance.dto.ComponentFunctionHistoryDto;
import com.neusoft.amos.maintenance.dto.ComponentMeasurePointDto;
import com.neusoft.amos.maintenance.dto.ComponentNodeDto;
import com.neusoft.amos.maintenance.dto.ComponentStatusLogDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Component 聚合服务：列表过滤、聚合读写、change-status 命令（写状态日志）、
 * status-log / archive 查询、hierarchy 树查询。
 */
@Service
@RequiredArgsConstructor
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentCounterRepository counterRepository;
    private final ComponentMeasurePointRepository measurePointRepository;
    private final ComponentStatusLogRepository statusLogRepository;
    private final ComponentFunctionHistoryRepository functionHistoryRepository;
    private final ComponentArchiveRepository archiveRepository;

    private static final String[] AUTO_STATUSES = {"In Use", "Available"};

    public List<ComponentDto> list(String installation, String department, String status,
                                   String typeNumber, String functionNo, String q) {
        Specification<Component> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (installation != null) ps.add(cb.equal(root.get("installation"), installation));
            if (department != null) ps.add(cb.equal(root.get("department"), department));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (typeNumber != null) ps.add(cb.like(root.get("typeNumber"), "%" + typeNumber + "%"));
            if (functionNo != null) ps.add(cb.equal(root.get("functionNo"), functionNo));
            if (q != null) {
                String like = "%" + q + "%";
                ps.add(cb.or(
                        cb.like(root.get("number"), like),
                        cb.like(root.get("name"), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return componentRepository.findAll(spec).stream().map(this::toDto).collect(Collectors.toList());
    }

    public ComponentDto get(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ComponentDto create(ComponentDto dto) {
        Component entity = new Component();
        applyDto(entity, dto);
        if (entity.getNumber() == null || entity.getNumber().isBlank()) {
            entity.setNumber("C-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        String today = LocalDate.now().toString();
        entity.setDateCreated(today);
        entity.setDateModified(today);
        return toDto(componentRepository.save(entity));
    }

    @Transactional
    public ComponentDto update(Long id, ComponentDto dto) {
        Component entity = findOrThrow(id);
        applyDto(entity, dto);
        entity.setId(id);
        entity.setDateModified(LocalDate.now().toString());
        return toDto(componentRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        componentRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> changeStatus(Long id, ChangeStatusRequest req) {
        Component comp = findOrThrow(id);
        String newStatus = req.getNewStatus();
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("newStatus is required");
        }
        String old = comp.getStatus();
        comp.setStatus(newStatus);
        componentRepository.save(comp);

        String changedBy = (req.getChangedBy() != null && !req.getChangedBy().isBlank())
                ? req.getChangedBy() : "A. Admin";
        writeStatusLog(comp, old, newStatus, req.getReason(), changedBy);

        List<Long> updatedIds = new ArrayList<>();
        updatedIds.add(comp.getId());

        if (req.isCascadeSubComponents()) {
            List<Component> children = componentRepository.findAll().stream()
                    .filter(c -> comp.getNumber() != null && comp.getNumber().equals(c.getParentComponent()))
                    .collect(Collectors.toList());
            for (Component child : children) {
                String co = child.getStatus();
                child.setStatus(newStatus);
                componentRepository.save(child);
                writeStatusLog(child, co, newStatus, "Cascade from " + comp.getNumber(), changedBy);
                updatedIds.add(child.getId());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("affectedWanted", new ArrayList<>());
        result.put("updatedIds", updatedIds);
        return result;
    }

    public List<ComponentStatusLogDto> getStatusLog(Long id) {
        Component comp = findOrThrow(id);
        return statusLogRepository.findByComponentIdOrderByChangedAtDescIdDesc(id).stream()
                .map(l -> toStatusLogDto(l, comp.getName()))
                .collect(Collectors.toList());
    }

    public List<ComponentStatusLogDto> getAllStatusLogs() {
        Map<Long, String> nameById = componentRepository.findAll().stream()
                .collect(Collectors.toMap(Component::getId, Component::getName, (a, b) -> a));
        return statusLogRepository.findAllByOrderByChangedAtDescIdDesc().stream()
                .map(l -> toStatusLogDto(l, nameById.get(l.getComponentId())))
                .collect(Collectors.toList());
    }

    public List<ComponentArchiveDto> getArchive(Long id, String kind) {
        Component comp = findOrThrow(id);
        List<ComponentArchive> archives = archiveRepository.findByComponentNoOrderByArchiveDateDescIdDesc(
                comp.getNumber());
        if (kind != null) {
            archives = archives.stream().filter(a -> kind.equals(a.getKind())).collect(Collectors.toList());
        }
        return archives.stream().map(this::toArchiveDto).collect(Collectors.toList());
    }

    public List<ComponentFunctionHistoryDto> getFunctionHistory(Long id) {
        return functionHistoryRepository.findByComponentIdOrderByPerformedAtDescIdDesc(id).stream()
                .map(this::toFunctionHistoryDto)
                .collect(Collectors.toList());
    }

    public List<ComponentNodeDto> getHierarchy() {
        List<Component> all = componentRepository.findAll();
        Map<String, ComponentNodeDto> byNumber = new HashMap<>();
        for (Component c : all) {
            ComponentNodeDto node = new ComponentNodeDto();
            node.setId(c.getId());
            node.setNumber(c.getNumber());
            node.setName(c.getName());
            node.setStatus(c.getStatus());
            node.setTypeNumber(c.getTypeNumber());
            node.setFunctionNo(c.getFunctionNo());
            node.setParentComponent(c.getParentComponent());
            byNumber.put(c.getNumber(), node);
        }
        List<ComponentNodeDto> roots = new ArrayList<>();
        for (Component c : all) {
            ComponentNodeDto node = byNumber.get(c.getNumber());
            if (c.getParentComponent() != null && !c.getParentComponent().isBlank()
                    && byNumber.containsKey(c.getParentComponent())) {
                byNumber.get(c.getParentComponent()).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    // ---- mapping ----

    private Component findOrThrow(Long id) {
        return componentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("component not found: " + id));
    }

    private void writeStatusLog(Component comp, String oldStatus, String newStatus, String reason, String changedBy) {
        ComponentStatusLog log = new ComponentStatusLog();
        log.setComponentId(comp.getId());
        log.setComponentNo(comp.getNumber());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setReason(reason != null ? reason : "");
        log.setChangedBy(changedBy);
        log.setChangedAt(LocalDate.now().toString());
        statusLogRepository.save(log);
    }

    private ComponentDto toDto(Component e) {
        ComponentDto d = new ComponentDto();
        d.setId(e.getId());
        d.setNumber(e.getNumber());
        d.setTypeNumber(e.getTypeNumber());
        d.setName(e.getName());
        d.setStatus(e.getStatus());
        d.setMaker(e.getMaker());
        d.setType(e.getType());
        d.setSerialNo(e.getSerialNo());
        d.setLocation(e.getLocation());
        d.setDepartment(e.getDepartment());
        d.setVendor(e.getVendor());
        d.setFunctionNo(e.getFunctionNo());
        d.setInstallDate(e.getInstallDate());
        d.setInstallation(e.getInstallation());
        d.setParentComponent(e.getParentComponent());
        d.setComponentTypeModel(e.getComponentTypeModel());
        d.setDateCreated(e.getDateCreated());
        d.setDateModified(e.getDateModified());

        d.setComponentCounters(e.getComponentCounters().stream().map(c -> {
            ComponentCounterDto cd = new ComponentCounterDto();
            cd.setId(c.getId());
            cd.setCode(c.getCode());
            cd.setDescription(c.getDescription());
            cd.setUnit(c.getUnit());
            cd.setCurrentValue(c.getCurrentValue());
            cd.setDependsOn(c.getDependsOn());
            cd.setLatestZeroedDate(c.getLatestZeroedDate());
            cd.setStartValue(c.getStartValue());
            cd.setAverage(c.getAverage());
            cd.setCalculate(c.getCalculate());
            return cd;
        }).collect(Collectors.toList()));

        d.setComponentMeasurePoints(e.getComponentMeasurePoints().stream().map(m -> {
            ComponentMeasurePointDto md = new ComponentMeasurePointDto();
            md.setId(m.getId());
            md.setCode(m.getCode());
            md.setDescription(m.getDescription());
            md.setUnit(m.getUnit());
            md.setTrend(m.getTrend());
            md.setValue(m.getValue());
            md.setLastReadDate(m.getLastReadDate());
            return md;
        }).collect(Collectors.toList()));

        return d;
    }

    private void applyDto(Component e, ComponentDto d) {
        e.setNumber(d.getNumber());
        e.setTypeNumber(d.getTypeNumber());
        e.setName(d.getName());
        e.setStatus(d.getStatus());
        e.setMaker(d.getMaker());
        e.setType(d.getType());
        e.setSerialNo(d.getSerialNo());
        e.setLocation(d.getLocation());
        e.setDepartment(d.getDepartment());
        e.setVendor(d.getVendor());
        e.setFunctionNo(d.getFunctionNo());
        e.setInstallDate(d.getInstallDate());
        e.setInstallation(d.getInstallation());
        e.setParentComponent(d.getParentComponent());
        e.setComponentTypeModel(d.getComponentTypeModel());
        e.setDateCreated(d.getDateCreated());
        e.setDateModified(d.getDateModified());

        Map<Long, ComponentCounter> exCounters = new HashMap<>();
        for (ComponentCounter c : e.getComponentCounters()) exCounters.put(c.getId(), c);
        e.getComponentCounters().clear();
        for (ComponentCounterDto cd : d.getComponentCounters()) {
            ComponentCounter c = (cd.getId() != null) ? exCounters.get(cd.getId()) : null;
            if (c == null) c = new ComponentCounter();
            c.setCode(cd.getCode());
            c.setDescription(cd.getDescription());
            c.setUnit(cd.getUnit());
            c.setCurrentValue(cd.getCurrentValue());
            c.setDependsOn(cd.getDependsOn());
            c.setLatestZeroedDate(cd.getLatestZeroedDate());
            c.setStartValue(cd.getStartValue());
            c.setAverage(cd.getAverage());
            c.setCalculate(cd.getCalculate());
            c.setComponent(e);
            e.getComponentCounters().add(c);
        }

        Map<Long, ComponentMeasurePoint> exMps = new HashMap<>();
        for (ComponentMeasurePoint m : e.getComponentMeasurePoints()) exMps.put(m.getId(), m);
        e.getComponentMeasurePoints().clear();
        for (ComponentMeasurePointDto md : d.getComponentMeasurePoints()) {
            ComponentMeasurePoint m = (md.getId() != null) ? exMps.get(md.getId()) : null;
            if (m == null) m = new ComponentMeasurePoint();
            m.setCode(md.getCode());
            m.setDescription(md.getDescription());
            m.setUnit(md.getUnit());
            m.setTrend(md.getTrend());
            m.setValue(md.getValue());
            m.setLastReadDate(md.getLastReadDate());
            m.setComponent(e);
            e.getComponentMeasurePoints().add(m);
        }
    }

    private ComponentStatusLogDto toStatusLogDto(ComponentStatusLog l, String name) {
        ComponentStatusLogDto d = new ComponentStatusLogDto();
        d.setId(l.getId());
        d.setComponentId(l.getComponentId());
        d.setComponentNo(l.getComponentNo());
        d.setComponentName(name);
        d.setOldStatus(l.getOldStatus());
        d.setNewStatus(l.getNewStatus());
        d.setReason(l.getReason());
        d.setChangedBy(l.getChangedBy());
        d.setChangedAt(l.getChangedAt());
        return d;
    }

    private ComponentArchiveDto toArchiveDto(ComponentArchive a) {
        ComponentArchiveDto d = new ComponentArchiveDto();
        d.setId(a.getId());
        d.setComponentNo(a.getComponentNo());
        d.setKind(a.getKind());
        d.setFromDepartment(a.getFromDepartment());
        d.setToDepartment(a.getToDepartment());
        d.setArchiveDate(a.getArchiveDate());
        d.setData(a.getData());
        return d;
    }

    private ComponentFunctionHistoryDto toFunctionHistoryDto(ComponentFunctionHistory h) {
        ComponentFunctionHistoryDto d = new ComponentFunctionHistoryDto();
        d.setId(h.getId());
        d.setComponentId(h.getComponentId());
        d.setComponentNo(h.getComponentNo());
        d.setFunctionNo(h.getFunctionNo());
        d.setFunctionDescription(h.getFunctionDescription());
        d.setLocation(h.getLocation());
        d.setAction(h.getAction());
        d.setPerformedBy(h.getPerformedBy());
        d.setPerformedAt(h.getPerformedAt());
        return d;
    }
}
