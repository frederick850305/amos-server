package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Long>,
        JpaSpecificationExecutor<Component> {

    Optional<Component> findByNumber(String number);
}
