package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FunctionRepository extends JpaRepository<MaintenanceFunction, Long>,
        JpaSpecificationExecutor<MaintenanceFunction> {

    Optional<MaintenanceFunction> findByFunctionNo(String functionNo);

    Optional<MaintenanceFunction> findByFunctionNoAndInstallation(String functionNo, String installation);

    List<MaintenanceFunction> findByParentFunctionNo(String parentFunctionNo);
}
