package com.neusoft.amos.system.service;

import com.neusoft.amos.system.domain.Installation;
import com.neusoft.amos.system.dto.InstallationDto;
import com.neusoft.amos.system.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallationService {

    private final InstallationRepository repository;

    public InstallationService(InstallationRepository repository) {
        this.repository = repository;
    }

    public List<InstallationDto> list() {
        return repository.findAll().stream()
                .map(e -> new InstallationDto(e.getId(), e.getCode(), e.getName(), e.getStatus()))
                .toList();
    }

    public InstallationDto get(Long id) {
        Installation e = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("installation not found: " + id));
        return new InstallationDto(e.getId(), e.getCode(), e.getName(), e.getStatus());
    }

    public InstallationDto create(InstallationDto dto) {
        if (repository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("installation code already exists: " + dto.code());
        }
        Installation e = new Installation();
        e.setCode(dto.code());
        e.setName(dto.name());
        e.setStatus(dto.status() == null ? "Active" : dto.status());
        e = repository.save(e);
        return new InstallationDto(e.getId(), e.getCode(), e.getName(), e.getStatus());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
