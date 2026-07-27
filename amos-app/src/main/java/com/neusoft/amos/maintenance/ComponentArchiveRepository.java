package com.neusoft.amos.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentArchiveRepository extends JpaRepository<ComponentArchive, Long> {
    List<ComponentArchive> findByComponentNoOrderByArchiveDateDescIdDesc(String componentNo);
}
