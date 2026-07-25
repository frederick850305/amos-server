package com.neusoft.amos.register;

import com.neusoft.amos.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register/makers")
public class MakerRegisterController
        extends AbstractCrudController<MakerRegister, Long, MakerRegisterRepository> {

    public MakerRegisterController(MakerRegisterRepository repository) {
        super(repository);
    }
}
