package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComponentTypeRepository extends JpaRepository<ComponentType, Long>,
        JpaSpecificationExecutor<ComponentType> {

    boolean existsByTypeNumber(String typeNumber);

    java.util.Optional<ComponentType> findByTypeNumber(String typeNumber);
}
