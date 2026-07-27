package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentFunctionHistoryRepository extends JpaRepository<ComponentFunctionHistory, Long> {
    List<ComponentFunctionHistory> findByComponentIdOrderByPerformedAtDescIdDesc(Long componentId);
    List<ComponentFunctionHistory> findByComponentNoOrderByPerformedAtDescIdDesc(String componentNo);
}
