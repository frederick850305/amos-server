package com.neusoft.amos.maintenance;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance/component-types")
public class ComponentTypeController
        extends AbstractCrudController<ComponentType, Long, ComponentTypeRepository> {

    public ComponentTypeController(ComponentTypeRepository repository) {
        super(repository);
    }
}
