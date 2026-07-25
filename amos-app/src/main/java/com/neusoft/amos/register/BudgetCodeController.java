package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/register/budget-codes")
public class BudgetCodeController extends RegisterController<BudgetCode, BudgetCodeRepository> {

    public BudgetCodeController(BudgetCodeRepository repository) {
        super(repository);
    }

    @Override
    protected List<String> searchableFields() {
        return List.of("code", "name", "description", "parentBudgetCode");
    }

    @Override
    protected String statusField() {
        return "status";
    }

    @Override
    protected void applyDeactivate(BudgetCode entity) {
        entity.setStatus("INACTIVE");
    }
}
