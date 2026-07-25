package com.neusoft.amos.system.repository;

import com.neusoft.amos.system.domain.UserDepartmentAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDepartmentAccessRepository extends JpaRepository<UserDepartmentAccess, Long> {
    List<UserDepartmentAccess> findByUserUsername(String username);

    List<UserDepartmentAccess> findByUserId(Long userId);
}
