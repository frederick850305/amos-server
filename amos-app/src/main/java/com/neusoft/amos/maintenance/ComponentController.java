package com.neusoft.amos.maintenance;

import com.neusoft.amos.maintenance.dto.ChangeStatusRequest;
import com.neusoft.amos.maintenance.dto.ComponentArchiveDto;
import com.neusoft.amos.maintenance.dto.ComponentDto;
import com.neusoft.amos.maintenance.dto.ComponentFunctionHistoryDto;
import com.neusoft.amos.maintenance.dto.ComponentNodeDto;
import com.neusoft.amos.maintenance.dto.ComponentStatusLogDto;
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
 * Component 显式端点。从 AbstractCrudController 改为聚合 DTO + 业务命令/查询端点。
 */
@RestController
@RequestMapping("/api/maintenance/components")
public class ComponentController {

    private final ComponentService service;

    public ComponentController(ComponentService service) {
        this.service = service;
    }

    @GetMapping
    public List<ComponentDto> list(
            @RequestParam(required = false) String installation,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String typeNumber,
            @RequestParam(required = false) String functionNo,
            @RequestParam(required = false) String q) {
        return service.list(installation, department, status, typeNumber, functionNo, q);
    }

    @GetMapping("/hierarchy")
    public List<ComponentNodeDto> hierarchy() {
        return service.getHierarchy();
    }

    @GetMapping("/{id}")
    public ComponentDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComponentDto create(@RequestBody ComponentDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ComponentDto update(@PathVariable Long id, @RequestBody ComponentDto dto) {
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

    @GetMapping("/{id}/status-log")
    public List<ComponentStatusLogDto> statusLog(@PathVariable Long id) {
        return service.getStatusLog(id);
    }

    @GetMapping("/{id}/function-history")
    public List<ComponentFunctionHistoryDto> functionHistory(@PathVariable Long id) {
        return service.getFunctionHistory(id);
    }

    @GetMapping("/{id}/archive")
    public List<ComponentArchiveDto> archive(@PathVariable Long id,
                                             @RequestParam(required = false) String kind) {
        return service.getArchive(id, kind);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
