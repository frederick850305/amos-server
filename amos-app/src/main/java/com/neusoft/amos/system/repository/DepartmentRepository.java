package com.neusoft.amos.system.repository;

import com.neusoft.amos.system.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByInstallationCode(String installationCode);

    List<Department> findByInstallationId(Long installationId);
}
