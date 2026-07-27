package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentStatusLogRepository extends JpaRepository<ComponentStatusLog, Long> {
    List<ComponentStatusLog> findByComponentIdOrderByChangedAtDescIdDesc(Long componentId);
    List<ComponentStatusLog> findByComponentNoOrderByChangedAtDescIdDesc(String componentNo);
    List<ComponentStatusLog> findAllByOrderByChangedAtDescIdDesc();
}
