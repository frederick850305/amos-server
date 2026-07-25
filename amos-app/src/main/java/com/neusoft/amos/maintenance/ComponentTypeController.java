package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.ComponentTypeDto;
import com.neusoft.amos.maintenance.dto.RegisterComponentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Component Type 显式端点。从 AbstractCrudController 改为聚合 DTO + 业务命令端点。
 */
@RestController
@RequestMapping("/api/maintenance/component-types")
public class ComponentTypeController {

    private final ComponentTypeService service;

    public ComponentTypeController(ComponentTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<ComponentTypeDto> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String maker,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String typeNumber,
            @RequestParam(required = false) String name) {
        return service.list(status, maker, classCode, typeNumber, name);
    }

    @GetMapping("/{id}")
    public ComponentTypeDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComponentTypeDto create(@RequestBody ComponentTypeDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ComponentTypeDto update(@PathVariable Long id, @RequestBody ComponentTypeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/register-component")
    public Component registerComponent(@PathVariable Long id, @RequestBody RegisterComponentRequest request) {
        return service.registerComponent(id, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
