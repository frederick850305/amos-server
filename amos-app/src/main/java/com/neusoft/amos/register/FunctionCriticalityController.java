package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/register/function-criticalities")
public class FunctionCriticalityController
        extends RegisterController<FunctionCriticality, FunctionCriticalityRepository> {

    public FunctionCriticalityController(FunctionCriticalityRepository repository) {
        super(repository);
    }

    @Override
    protected List<String> searchableFields() {
        return List.of("degree", "description");
    }

    @Override
    protected String statusField() {
        return "active";
    }

    @Override
    protected void applyDeactivate(FunctionCriticality entity) {
        entity.setActive(false);
    }

    /** 手册要求按 active 布尔过滤；查询级叠加，保证分页正确。 */
    @Override
    protected Specification<FunctionCriticality> applyExtraSpec(Specification<FunctionCriticality> spec,
                                                               Long installation,
                                                               Long parentId,
                                                               Boolean active) {
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        return spec;
    }
}
