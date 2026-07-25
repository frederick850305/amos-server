package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/register/disciplines")
public class DisciplineController extends RegisterController<Discipline, DisciplineRepository> {

    public DisciplineController(DisciplineRepository repository) {
        super(repository);
    }

    @Override
    protected List<String> searchableFields() {
        return List.of("code", "name", "description");
    }

    @Override
    protected String statusField() {
        return "status";
    }

    @Override
    protected void applyDeactivate(Discipline entity) {
        entity.setStatus("INACTIVE");
    }
}
