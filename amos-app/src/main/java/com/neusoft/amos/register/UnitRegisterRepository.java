package com.neusoft.amos.register;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UnitRegisterRepository
        extends JpaRepository<UnitRegister, Long>, JpaSpecificationExecutor<UnitRegister> {
}
