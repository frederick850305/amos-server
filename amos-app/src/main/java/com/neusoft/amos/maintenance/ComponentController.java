package com.neusoft.amos.maintenance;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance/components")
public class ComponentController
        extends AbstractCrudController<Component, Long, ComponentRepository> {

    public ComponentController(ComponentRepository repository) {
        super(repository);
    }
}
