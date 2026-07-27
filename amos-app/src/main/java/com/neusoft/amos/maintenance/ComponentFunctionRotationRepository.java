package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentFunctionRotationRepository extends JpaRepository<ComponentFunctionRotation, Long> {
    List<ComponentFunctionRotation> findByFunctionIdOrderByPerformedAtDescIdDesc(Long functionId);
    List<ComponentFunctionRotation> findByComponentNoOrderByPerformedAtDescIdDesc(String componentNo);
}
