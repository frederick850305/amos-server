package com.neusoft.amos.system.web;

import com.neusoft.amos.system.dto.DepartmentDto;
import com.neusoft.amos.system.service.DepartmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<DepartmentDto> list(@RequestParam String installation) {
        return service.listByInstallation(installation);
    }

    @PostMapping
    public DepartmentDto create(@RequestBody DepartmentDto dto) {
        return service.create(dto);
    }
}
