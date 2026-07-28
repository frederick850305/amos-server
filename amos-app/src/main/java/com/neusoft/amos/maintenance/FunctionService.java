package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Function 聚合服务：列表过滤、聚合读写、change-status 命令、
 * install/remove-component 业务命令（联动维护 component 状态并写轮次/历史日志）、
 * rotation-log 查询。install/remove 直接操作 ComponentRepository 以保证一次请求原子完成。
 */
@Service
@RequiredArgsConstructor
public class FunctionService {

    private final FunctionRepository functionRepository;
    private final FunctionCounterRepository counterRepository;
    private final ComponentFunctionRotationRepository rotationRepository;
    private final ComponentFunctionHistoryRepository functionHistoryRepository;
    private final ComponentRepository componentRepository;
    private final ComponentStatusLogRepository statusLogRepository;

    private static final String PERFORMED_BY = "A. Admin";

    public List<FunctionDto> list(String installation, String department, String status,
                                  String parentFunctionNo, String criticality, String q) {
        Specification<MaintenanceFunction> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (installation != null) ps.add(cb.equal(root.get("installation"), installation));
            if (department != null) ps.add(cb.equal(root.get("department"), department));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (parentFunctionNo != null) ps.add(cb.equal(root.get("parentFunctionNo"), parentFunctionNo));
            if (criticality != null) ps.add(cb.equal(root.get("criticality"), criticality));
            if (q != null) {
                String like = "%" + q + "%";
                ps.add(cb.or(
                        cb.like(root.get("functionNo"), like),
                        cb.like(root.get("description"), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return functionRepository.findAll(spec).stream().map(this::toDto).collect(Collectors.toList());
    }

    public FunctionDto get(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public FunctionDto create(FunctionDto dto) {
        MaintenanceFunction e = new MaintenanceFunction();
        applyDto(e, dto);
        e.setId(null);
        return toDto(functionRepository.save(e));
    }

    @Transactional
    public FunctionDto update(Long id, FunctionDto dto) {
        MaintenanceFunction e = findOrThrow(id);
        applyDto(e, dto);
        e.setId(id);
        return toDto(functionRepository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        functionRepository.deleteById(id);
    }

    // ---- 状态变更：装有组件的 function 不能改状态 ----
    @Transactional
    public Map<String, Object> changeStatus(Long id, ChangeStatusRequest req) {
        MaintenanceFunction fn = findOrThrow(id);
        String newStatus = req.getNewStatus();
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("newStatus is required");
        }
        if (fn.getInstalledComponentNo() != null && !fn.getInstalledComponentNo().isBlank()) {
            throw new IllegalArgumentException("cannot change status of function with installed component");
        }
        fn.setStatus(newStatus);
        List<Long> updatedIds = new ArrayList<>();
        updatedIds.add(fn.getId());
        if (Boolean.TRUE.equals(req.getCascadeSubFunctions())) {
            for (MaintenanceFunction sub : functionRepository.findByParentFunctionNo(fn.getFunctionNo())) {
                if (sub.getInstalledComponentNo() == null || sub.getInstalledComponentNo().isBlank()) {
                    sub.setStatus(newStatus);
                    updatedIds.add(sub.getId());
                }
            }
        }
        functionRepository.saveAll(updatedIds.stream().map(this::findOrThrow).collect(Collectors.toList()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("updatedIds", updatedIds);
        return result;
    }

    // ---- 安装组件：联动维护 component 状态，写轮次/历史/状态日志 ----
    @Transactional
    public FunctionDto installComponent(Long id, InstallComponentRequest req) {
        MaintenanceFunction fn = findOrThrow(id);
        String componentNumber = req.getComponentNumber();
        if (componentNumber == null || componentNumber.isBlank()) {
            throw new IllegalArgumentException("componentNumber is required");
        }
        Component comp = componentRepository.findByNumber(componentNumber)
                .orElseThrow(() -> new IllegalArgumentException("component not found: " + componentNumber));
        // 组件已装在别的 function 上 -> 必须先显式拆卸（不允许隐式跨位置搬迁）
        if (comp.getFunctionNo() != null && !comp.getFunctionNo().isBlank()
                && !comp.getFunctionNo().equals(fn.getFunctionNo())) {
            throw new IllegalArgumentException(
                    "component already installed on function " + comp.getFunctionNo() + ", remove it first");
        }
        // 同一组件重复装到同一 function -> 明确错误
        if (comp.getFunctionNo() != null && comp.getFunctionNo().equals(fn.getFunctionNo())
                && fn.getInstalledComponentNo() != null && fn.getInstalledComponentNo().equals(componentNumber)) {
            throw new IllegalArgumentException("component already installed on this function");
        }
        // 若已装其他组件，先拆卸旧的（回落 Available）
        if (fn.getInstalledComponentNo() != null && !fn.getInstalledComponentNo().equals(componentNumber)) {
            detachComponent(fn, "", "", "");
        }
        fn.setInstalledComponentNo(componentNumber);
        comp.setFunctionNo(fn.getFunctionNo());
        String oldStatus = comp.getStatus();
        comp.setStatus("In Use");
        comp.setLocation(fn.getLocation());
        componentRepository.save(comp);
        writeStatusLog(comp, oldStatus, "In Use", "Installed on " + fn.getFunctionNo());
        writeRotation(fn, comp.getId(), comp.getNumber(), comp.getName(), "Installed",
                req.getDetails(), "", "", LocalDate.now().toString(), "");
        writeHistory(comp, fn, "Installed", req.getDetails());
        return toDto(functionRepository.save(fn));
    }

    // ---- 拆卸组件：清空 function 安装组件，联动维护 component 状态 ----
    @Transactional
    public FunctionDto removeComponent(Long id, RemoveComponentRequest req) {
        MaintenanceFunction fn = findOrThrow(id);
        // 主 function 未装组件且未级联 -> 明确错误
        if ((fn.getInstalledComponentNo() == null || fn.getInstalledComponentNo().isBlank())
                && !Boolean.TRUE.equals(req.getCascadeSubFunctions())) {
            throw new IllegalArgumentException("no component installed on function " + fn.getFunctionNo());
        }
        detachComponent(fn, req.getNewLocation(), req.getStatus(), req.getDetails());
        if (Boolean.TRUE.equals(req.getCascadeSubFunctions())) {
            for (MaintenanceFunction sub : functionRepository.findByParentFunctionNo(fn.getFunctionNo())) {
                if (sub.getInstalledComponentNo() != null && !sub.getInstalledComponentNo().isBlank()) {
                    detachComponent(sub, req.getNewLocation(), req.getStatus(), req.getDetails());
                }
            }
        }
        return toDto(functionRepository.save(fn));
    }

    public List<FunctionRotationDto> getRotationLog(Long id) {
        findOrThrow(id);
        return rotationRepository.findByFunctionIdOrderByPerformedAtDescIdDesc(id).stream()
                .map(this::toRotationDto)
                .collect(Collectors.toList());
    }

    // ---- 联动：拆卸单个 function 上的组件（含日志） ----
    private void detachComponent(MaintenanceFunction fn, String newLocation, String status, String details) {
        if (fn.getInstalledComponentNo() == null || fn.getInstalledComponentNo().isBlank()) return;
        String compNo = fn.getInstalledComponentNo();
        Component comp = componentRepository.findByNumber(compNo).orElse(null);
        fn.setInstalledComponentNo(null);
        if (comp != null) {
            String oldStatus = comp.getStatus();
            comp.setFunctionNo("");
            String newStatus;
            if (status != null && !status.isBlank()) newStatus = status;
            else newStatus = "Available";
            comp.setStatus(newStatus);
            if (newLocation != null && !newLocation.isBlank()) comp.setLocation(newLocation);
            componentRepository.save(comp);
            writeStatusLog(comp, oldStatus, newStatus, "Removed from " + fn.getFunctionNo());
        }
        writeRotation(fn, comp != null ? comp.getId() : null, compNo,
                comp != null ? comp.getName() : "", "Removed",
                details, newLocation, status, "", LocalDate.now().toString());
        if (comp != null) writeHistory(comp, fn, "Removed", details);
    }

    private void writeRotation(MaintenanceFunction fn, Long componentId, String componentNo, String componentName,
                               String action, String details, String newLocation, String newStatus,
                               String installedAt, String removedAt) {
        ComponentFunctionRotation r = new ComponentFunctionRotation();
        r.setFunctionId(fn.getId());
        r.setComponentId(componentId);
        r.setComponentNo(componentNo);
        r.setComponentName(componentName);
        r.setAction(action);
        r.setPerformedBy(PERFORMED_BY);
        r.setPerformedAt(LocalDate.now().toString());
        r.setDetails(details != null ? details : "");
        r.setNewLocation(newLocation != null ? newLocation : "");
        r.setNewStatus(newStatus != null ? newStatus : "");
        r.setInstalledAt(installedAt != null ? installedAt : "");
        r.setRemovedAt(removedAt != null ? removedAt : "");
        rotationRepository.save(r);
    }

    private void writeStatusLog(Component comp, String oldStatus, String newStatus, String reason) {
        ComponentStatusLog log = new ComponentStatusLog();
        log.setComponentId(comp.getId());
        log.setComponentNo(comp.getNumber());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setReason(reason != null ? reason : "");
        log.setChangedBy(PERFORMED_BY);
        log.setChangedAt(LocalDate.now().toString());
        statusLogRepository.save(log);
    }

    private void writeHistory(Component comp, MaintenanceFunction fn, String action, String details) {
        ComponentFunctionHistory h = new ComponentFunctionHistory();
        h.setComponentId(comp.getId());
        h.setComponentNo(comp.getNumber());
        h.setFunctionNo(fn.getFunctionNo());
        h.setFunctionDescription(fn.getDescription());
        h.setLocation(fn.getLocation());
        h.setAction(action);
        h.setPerformedBy(PERFORMED_BY);
        h.setPerformedAt(LocalDate.now().toString());
        functionHistoryRepository.save(h);
    }

    private MaintenanceFunction findOrThrow(Long id) {
        return functionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("function not found: " + id));
    }

    // ---- mapping ----
    private FunctionDto toDto(MaintenanceFunction e) {
        FunctionDto d = new FunctionDto();
        d.setId(e.getId());
        d.setFunctionNo(e.getFunctionNo());
        d.setInstallation(e.getInstallation());
        d.setDepartment(e.getDepartment());
        d.setDescription(e.getDescription());
        d.setReference(e.getReference());
        d.setParentFunctionNo(e.getParentFunctionNo());
        d.setStatus(e.getStatus());
        d.setLocation(e.getLocation());
        d.setCriticality(e.getCriticality());
        d.setInstalledComponentId(e.getInstalledComponentNo());

        d.setSfiCode(e.getSfiCode());
        d.setSystem(e.getSystem());
        d.setSubSystem(e.getSubSystem());
        d.setRemarks(e.getRemarks());
        d.setSerialNo(e.getSerialNo());
        d.setMaker(e.getMaker());
        d.setModel(e.getModel());
        d.setTagNo(e.getTagNo());
        d.setAssetValue(e.getAssetValue());
        d.setAcquisitionDate(e.getAcquisitionDate());
        d.setCurrency(e.getCurrency());
        d.setDepreciation(e.getDepreciation());

        d.setFunctionCounters(e.getFunctionCounters().stream().map(c -> {
            FunctionCounterDto cd = new FunctionCounterDto();
            cd.setId(c.getId());
            cd.setCode(c.getCode());
            cd.setDescription(c.getDescription());
            cd.setUnit(c.getUnit());
            cd.setLastValue(c.getLastValue());
            return cd;
        }).collect(Collectors.toList()));

        d.setRotationLog(rotationRepository.findByFunctionIdOrderByPerformedAtDescIdDesc(e.getId()).stream()
                .map(this::toRotationDto).collect(Collectors.toList()));

        return d;
    }

    private void applyDto(MaintenanceFunction e, FunctionDto d) {
        e.setFunctionNo(d.getFunctionNo());
        e.setInstallation(d.getInstallation());
        e.setDepartment(d.getDepartment());
        e.setDescription(d.getDescription());
        e.setReference(d.getReference());
        e.setParentFunctionNo(d.getParentFunctionNo());
        e.setStatus(d.getStatus());
        e.setLocation(d.getLocation());
        e.setCriticality(d.getCriticality());
        // installedComponentId / installedComponentNo 由 install/remove 命令维护，不在 applyDto 改写

        e.setSfiCode(d.getSfiCode());
        e.setSystem(d.getSystem());
        e.setSubSystem(d.getSubSystem());
        e.setRemarks(d.getRemarks());
        e.setSerialNo(d.getSerialNo());
        e.setMaker(d.getMaker());
        e.setModel(d.getModel());
        e.setTagNo(d.getTagNo());
        e.setAssetValue(d.getAssetValue());
        e.setAcquisitionDate(d.getAcquisitionDate());
        e.setCurrency(d.getCurrency());
        e.setDepreciation(d.getDepreciation());

        // 计数器子表：复用已有 id 规避 orphanRemoval 唯一约束冲突
        Map<Long, FunctionCounter> ex = new HashMap<>();
        for (FunctionCounter c : e.getFunctionCounters()) ex.put(c.getId(), c);
        e.getFunctionCounters().clear();
        for (FunctionCounterDto cd : d.getFunctionCounters()) {
            FunctionCounter c = (cd.getId() != null) ? ex.get(cd.getId()) : null;
            if (c == null) c = new FunctionCounter();
            c.setCode(cd.getCode());
            c.setDescription(cd.getDescription());
            c.setUnit(cd.getUnit());
            c.setLastValue(cd.getLastValue());
            c.setFunction(e);
            e.getFunctionCounters().add(c);
        }
    }

    private FunctionRotationDto toRotationDto(ComponentFunctionRotation r) {
        FunctionRotationDto d = new FunctionRotationDto();
        d.setId(r.getId());
        d.setComponentNo(r.getComponentNo());
        d.setComponentName(r.getComponentName());
        d.setAction(r.getAction());
        d.setPerformedBy(r.getPerformedBy());
        d.setPerformedAt(r.getPerformedAt());
        d.setDetails(r.getDetails());
        d.setNewLocation(r.getNewLocation());
        d.setNewStatus(r.getNewStatus());
        d.setInstalledAt(r.getInstalledAt());
        d.setRemovedAt(r.getRemovedAt());
        return d;
    }
}
