package com.neusoft.amos.system.repository;

import com.neusoft.amos.system.domain.UserOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserOptionRepository extends JpaRepository<UserOption, Long> {
    Optional<UserOption> findByUserIdAndOptKey(Long userId, String optKey);

    List<UserOption> findByUserId(Long userId);
}
