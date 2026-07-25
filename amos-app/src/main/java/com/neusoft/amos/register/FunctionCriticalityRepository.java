package com.neusoft.amos.register;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FunctionCriticalityRepository
        extends JpaRepository<FunctionCriticality, Long>, JpaSpecificationExecutor<FunctionCriticality> {
}
