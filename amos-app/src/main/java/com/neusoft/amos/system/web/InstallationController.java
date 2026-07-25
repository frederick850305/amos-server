package com.neusoft.amos.system.web;

import com.neusoft.amos.system.dto.InstallationDto;
import com.neusoft.amos.system.service.InstallationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/installations")
public class InstallationController {

    private final InstallationService service;

    public InstallationController(InstallationService service) {
        this.service = service;
    }

    @GetMapping
    public List<InstallationDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public InstallationDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public InstallationDto create(@RequestBody InstallationDto dto) {
        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
