package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/register/currencies")
public class CurrencyRegisterController extends RegisterController<CurrencyRegister, CurrencyRegisterRepository> {

    public CurrencyRegisterController(CurrencyRegisterRepository repository) {
        super(repository);
    }

    @Override
    protected List<String> searchableFields() {
        return List.of("code", "name", "symbol");
    }

    @Override
    protected String statusField() {
        return "status";
    }

    @Override
    protected void applyDeactivate(CurrencyRegister entity) {
        entity.setStatus("INACTIVE");
    }
}
