package com.neusoft.amos.register;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BudgetCodeRepository
        extends JpaRepository<BudgetCode, Long>, JpaSpecificationExecutor<BudgetCode> {
}
