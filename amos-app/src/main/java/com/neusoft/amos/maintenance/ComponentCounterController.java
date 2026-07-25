package com.neusoft.amos.maintenance;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance/component-counters")
public class ComponentCounterController
        extends AbstractCrudController<ComponentCounter, Long, ComponentCounterRepository> {

    public ComponentCounterController(ComponentCounterRepository repository) {
        super(repository);
    }
}
