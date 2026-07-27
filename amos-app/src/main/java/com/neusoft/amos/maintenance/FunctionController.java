package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.ChangeStatusRequest;
import com.neusoft.amos.maintenance.dto.FunctionDto;
import com.neusoft.amos.maintenance.dto.FunctionRotationDto;
import com.neusoft.amos.maintenance.dto.InstallComponentRequest;
import com.neusoft.amos.maintenance.dto.RemoveComponentRequest;
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
 * Function 显式端点，风格对齐 ComponentController。
 */
@RestController
@RequestMapping("/api/maintenance/functions")
public class FunctionController {

    private final FunctionService service;

    public FunctionController(FunctionService service) {
        this.service = service;
    }

    @GetMapping
    public List<FunctionDto> list(
            @RequestParam(required = false) String installation,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String parentFunctionNo,
            @RequestParam(required = false) String criticality,
            @RequestParam(required = false) String q) {
        return service.list(installation, department, status, parentFunctionNo, criticality, q);
    }

    @GetMapping("/{id}")
    public FunctionDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FunctionDto create(@RequestBody FunctionDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public FunctionDto update(@PathVariable Long id, @RequestBody FunctionDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/change-status")
    public Map<String, Object> changeStatus(@PathVariable Long id, @RequestBody ChangeStatusRequest request) {
        return service.changeStatus(id, request);
    }

    @PostMapping("/{id}/install-component")
    public FunctionDto installComponent(@PathVariable Long id, @RequestBody InstallComponentRequest request) {
        return service.installComponent(id, request);
    }

    @PostMapping("/{id}/remove-component")
    public FunctionDto removeComponent(@PathVariable Long id, @RequestBody RemoveComponentRequest request) {
        return service.removeComponent(id, request);
    }

    @GetMapping("/{id}/rotation-log")
    public List<FunctionRotationDto> rotationLog(@PathVariable Long id) {
        return service.getRotationLog(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
