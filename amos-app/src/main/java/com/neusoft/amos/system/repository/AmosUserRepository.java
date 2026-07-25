package com.neusoft.amos.system.repository;

import com.neusoft.amos.system.domain.AmosUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmosUserRepository extends JpaRepository<AmosUser, Long> {
    Optional<AmosUser> findByUsername(String username);
}
