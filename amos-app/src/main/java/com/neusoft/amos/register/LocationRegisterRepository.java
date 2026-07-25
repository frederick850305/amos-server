package com.neusoft.amos.register;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LocationRegisterRepository
        extends JpaRepository<LocationRegister, Long>, JpaSpecificationExecutor<LocationRegister> {

    List<LocationRegister> findByInstallationId(Long installationId);

    List<LocationRegister> findByParentLocationId(Long parentLocationId);
}
