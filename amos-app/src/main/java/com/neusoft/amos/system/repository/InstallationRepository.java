package com.neusoft.amos.system.repository;

import com.neusoft.amos.system.domain.Installation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstallationRepository extends JpaRepository<Installation, Long> {
    Optional<Installation> findByCode(String code);

    boolean existsByCode(String code);
}
