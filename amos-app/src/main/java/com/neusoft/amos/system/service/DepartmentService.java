package com.neusoft.amos.system.service;

import com.neusoft.amos.system.domain.Department;
import com.neusoft.amos.system.domain.Installation;
import com.neusoft.amos.system.dto.DepartmentDto;
import com.neusoft.amos.system.repository.DepartmentRepository;
import com.neusoft.amos.system.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;
    private final InstallationRepository installationRepository;

    public DepartmentService(DepartmentRepository repository, InstallationRepository installationRepository) {
        this.repository = repository;
        this.installationRepository = installationRepository;
    }

    public List<DepartmentDto> listByInstallation(String installationCode) {
        return repository.findByInstallationCode(installationCode).stream().map(this::toDto).toList();
    }

    public DepartmentDto create(DepartmentDto dto) {
        Installation installation = installationRepository.findByCode(dto.installationCode())
                .orElseThrow(() -> new IllegalArgumentException("installation not found: " + dto.installationCode()));
        Department e = new Department();
        e.setInstallation(installation);
        e.setCode(dto.code());
        e.setName(dto.name());
        e.setStatus(dto.status() == null ? "Active" : dto.status());
        e = repository.save(e);
        return toDto(e);
    }

    private DepartmentDto toDto(Department e) {
        return new DepartmentDto(
                e.getId(),
                e.getInstallation().getCode(),
                e.getInstallation().getName(),
                e.getCode(),
                e.getName(),
                e.getStatus());
    }
}
