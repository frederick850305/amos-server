package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

    /** 手册要求按 active 布尔过滤；叠加在通用 status(active) 过滤之上。 */
    @Override
    protected List<FunctionCriticality> applyExtraFilters(List<FunctionCriticality> result,
                                                          Long installation,
                                                          Long parentId,
                                                          Boolean active) {
        if (active != null) {
            return result.stream().filter(f -> active.equals(f.getActive())).collect(Collectors.toList());
        }
        return result;
    }
}
