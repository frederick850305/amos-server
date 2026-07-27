package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.ComponentTypeCounterDefDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeMeasurePointDefDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeRelatedTypeDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeStockTypeDto;
import com.neusoft.amos.maintenance.dto.RegisterComponentRequest;
import com.neusoft.amos.stock.StockType;
import com.neusoft.amos.stock.StockTypeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Component Type 聚合服务：列表过滤、聚合读写、唯一性校验、register-component 命令。
 */
@Service
@RequiredArgsConstructor
public class ComponentTypeService {

    private final ComponentTypeRepository typeRepository;
    private final ComponentRepository componentRepository;
    private final ComponentCounterRepository counterRepository;
    private final StockTypeRepository stockTypeRepository;

    public List<ComponentTypeDto> list(String status, String maker, String classCode,
                                       String typeNumber, String name) {
        Specification<ComponentType> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (maker != null) ps.add(cb.equal(root.get("maker"), maker));
            if (classCode != null) ps.add(cb.equal(root.get("classCode"), classCode));
            if (typeNumber != null) ps.add(cb.like(root.get("typeNumber"), "%" + typeNumber + "%"));
            if (name != null) ps.add(cb.like(root.get("name"), "%" + name + "%"));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return typeRepository.findAll(spec).stream().map(this::toDto).collect(Collectors.toList());
    }

    public ComponentTypeDto get(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ComponentTypeDto create(ComponentTypeDto dto) {
        if (dto.getTypeNumber() != null && typeRepository.existsByTypeNumber(dto.getTypeNumber())) {
            throw new IllegalArgumentException("typeNumber already exists: " + dto.getTypeNumber());
        }
        ComponentType entity = new ComponentType();
        applyDto(entity, dto);
        return toDto(typeRepository.save(entity));
    }

    @Transactional
    public ComponentTypeDto update(Long id, ComponentTypeDto dto) {
        ComponentType entity = findOrThrow(id);
        if (dto.getTypeNumber() != null && !dto.getTypeNumber().equals(entity.getTypeNumber())
                && typeRepository.existsByTypeNumber(dto.getTypeNumber())) {
            throw new IllegalArgumentException("typeNumber already exists: " + dto.getTypeNumber());
        }
        applyDto(entity, dto);
        return toDto(typeRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        typeRepository.deleteById(id);
    }

    @Transactional
    public Component registerComponent(Long id, RegisterComponentRequest req) {
        ComponentType type = findOrThrow(id);
        Component component = new Component();
        component.setNumber(req.getNumber());
        component.setName(req.getName());
        component.setTypeNumber(type.getTypeNumber());
        component.setMaker(type.getMaker());
        component.setType(type.getType());
        component.setLocation(req.getLocation());
        component.setDepartment(req.getDepartment());
        component.setInstallation(req.getInstallation());
        component.setSerialNo(req.getSerialNo());
        component.setStatus("Available");
        for (ComponentTypeCounterDef def : type.getCounters()) {
            ComponentCounter cc = new ComponentCounter();
            cc.setCode(def.getCode());
            cc.setDescription(def.getDescription());
            cc.setUnit(def.getUnit());
            component.getComponentCounters().add(cc);
        }
        return componentRepository.save(component);
    }

    // ---- mapping ----

    private ComponentType findOrThrow(Long id) {
        return typeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("component type not found: " + id));
    }

    private ComponentTypeDto toDto(ComponentType e) {
        ComponentTypeDto d = new ComponentTypeDto();
        d.setId(e.getId());
        d.setTypeNumber(e.getTypeNumber());
        d.setName(e.getName());
        d.setMaker(e.getMaker());
        d.setModel(e.getModel());
        d.setType(e.getType());
        d.setClassCode(e.getClassCode());
        d.setPreferredVendor(e.getPreferredVendor());
        d.setParentTypeNumber(e.getParentTypeNumber());
        d.setCompTypeModel(e.getCompTypeModel());
        d.setDescription(e.getDescription());
        d.setStatus(e.getStatus());
        d.setDateCreated(e.getDateCreated());
        d.setDateModified(e.getDateModified());

        d.setCounters(e.getCounters().stream().map(c -> {
            ComponentTypeCounterDefDto dto = new ComponentTypeCounterDefDto();
            dto.setId(c.getId());
            dto.setCode(c.getCode());
            dto.setDescription(c.getDescription());
            dto.setUnit(c.getUnit());
            dto.setSortOrder(c.getSortOrder());
            return dto;
        }).collect(Collectors.toList()));

        d.setMeasurePointDefs(e.getMeasurePointDefs().stream().map(m -> {
            ComponentTypeMeasurePointDefDto dto = new ComponentTypeMeasurePointDefDto();
            dto.setId(m.getId());
            dto.setCode(m.getCode());
            dto.setDescription(m.getDescription());
            dto.setTrend(m.getTrend());
            dto.setUnit(m.getUnit());
            dto.setSortOrder(m.getSortOrder());
            return dto;
        }).collect(Collectors.toList()));

        d.setRelatedTypes(e.getRelatedTypes().stream().map(r -> {
            ComponentTypeRelatedTypeDto dto = new ComponentTypeRelatedTypeDto();
            dto.setId(r.getId());
            if (r.getRelatedComponentType() != null) {
                dto.setRelatedComponentTypeId(r.getRelatedComponentType().getId());
                dto.setRelatedTypeNumber(r.getRelatedComponentType().getTypeNumber());
                dto.setRelatedTypeName(r.getRelatedComponentType().getName());
            }
            return dto;
        }).collect(Collectors.toList()));

        d.setStockTypeLinks(e.getStockTypeLinks().stream().map(s -> {
            ComponentTypeStockTypeDto dto = new ComponentTypeStockTypeDto();
            dto.setId(s.getId());
            if (s.getStockType() != null) {
                dto.setStockTypeId(s.getStockType().getId());
                dto.setStockTypeNo(s.getStockType().getStockTypeNo());
                dto.setDescription(s.getStockType().getDescription());
            }
            dto.setQuantity(s.getQuantity());
            dto.setMakersRef(s.getMakersRef());
            dto.setRemarks(s.getRemarks());
            dto.setAlternativeNo(s.getAlternativeNo());
            return dto;
        }).collect(Collectors.toList()));

        return d;
    }

    private void applyDto(ComponentType e, ComponentTypeDto d) {
        e.setTypeNumber(d.getTypeNumber());
        e.setName(d.getName());
        e.setMaker(d.getMaker());
        e.setModel(d.getModel());
        e.setType(d.getType());
        e.setClassCode(d.getClassCode());
        e.setPreferredVendor(d.getPreferredVendor());
        e.setParentTypeNumber(d.getParentTypeNumber());
        e.setCompTypeModel(d.getCompTypeModel());
        e.setDescription(d.getDescription());
        e.setStatus(d.getStatus());
        e.setDateCreated(d.getDateCreated());
        e.setDateModified(d.getDateModified());

        Map<Long, ComponentTypeCounterDef> exCounters = new HashMap<>();
        for (ComponentTypeCounterDef c : e.getCounters()) exCounters.put(c.getId(), c);
        e.getCounters().clear();
        for (ComponentTypeCounterDefDto cd : d.getCounters()) {
            ComponentTypeCounterDef c = (cd.getId() != null) ? exCounters.get(cd.getId()) : null;
            if (c == null) c = new ComponentTypeCounterDef();
            c.setCode(cd.getCode());
            c.setDescription(cd.getDescription());
            c.setUnit(cd.getUnit());
            c.setSortOrder(cd.getSortOrder());
            c.setComponentType(e);
            e.getCounters().add(c);
        }

        Map<Long, ComponentTypeMeasurePointDef> exMps = new HashMap<>();
        for (ComponentTypeMeasurePointDef m : e.getMeasurePointDefs()) exMps.put(m.getId(), m);
        e.getMeasurePointDefs().clear();
        for (ComponentTypeMeasurePointDefDto md : d.getMeasurePointDefs()) {
            ComponentTypeMeasurePointDef m = (md.getId() != null) ? exMps.get(md.getId()) : null;
            if (m == null) m = new ComponentTypeMeasurePointDef();
            m.setCode(md.getCode());
            m.setDescription(md.getDescription());
            m.setTrend(md.getTrend());
            m.setUnit(md.getUnit());
            m.setSortOrder(md.getSortOrder());
            m.setComponentType(e);
            e.getMeasurePointDefs().add(m);
        }

        Map<Long, ComponentTypeRelatedType> exRel = new HashMap<>();
        for (ComponentTypeRelatedType r : e.getRelatedTypes()) exRel.put(r.getId(), r);
        e.getRelatedTypes().clear();
        for (ComponentTypeRelatedTypeDto rd : d.getRelatedTypes()) {
            ComponentTypeRelatedType r = (rd.getId() != null) ? exRel.get(rd.getId()) : null;
            if (r == null) r = new ComponentTypeRelatedType();
            if (rd.getRelatedComponentTypeId() != null) {
                ComponentType rel = new ComponentType();
                rel.setId(rd.getRelatedComponentTypeId());
                r.setRelatedComponentType(rel);
            } else if (rd.getRelatedTypeNumber() != null) {
                ComponentType rel = typeRepository.findByTypeNumber(rd.getRelatedTypeNumber())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "related component type not found: " + rd.getRelatedTypeNumber()));
                r.setRelatedComponentType(rel);
            } else {
                r.setRelatedComponentType(null);
            }
            r.setComponentType(e);
            e.getRelatedTypes().add(r);
        }

        Map<Long, ComponentTypeStockType> exStock = new HashMap<>();
        for (ComponentTypeStockType s : e.getStockTypeLinks()) exStock.put(s.getId(), s);
        e.getStockTypeLinks().clear();
        for (ComponentTypeStockTypeDto sd : d.getStockTypeLinks()) {
            ComponentTypeStockType s = (sd.getId() != null) ? exStock.get(sd.getId()) : null;
            if (s == null) s = new ComponentTypeStockType();
            if (sd.getStockTypeId() != null) {
                StockType st = new StockType();
                st.setId(sd.getStockTypeId());
                s.setStockType(st);
            } else if (sd.getStockTypeNo() != null) {
                StockType st = stockTypeRepository.findByStockTypeNo(sd.getStockTypeNo())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "stock type not found: " + sd.getStockTypeNo()));
                s.setStockType(st);
            } else {
                s.setStockType(null);
            }
            s.setAlternativeNo(sd.getAlternativeNo());
            s.setQuantity(sd.getQuantity());
            s.setMakersRef(sd.getMakersRef());
            s.setRemarks(sd.getRemarks());
            s.setComponentType(e);
            e.getStockTypeLinks().add(s);
        }
    }
}
