package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/register/vendors")
public class VendorRegisterController extends RegisterController<VendorRegister, VendorRegisterRepository> {

    public VendorRegisterController(VendorRegisterRepository repository) {
        super(repository);
    }

    @Override
    protected List<String> searchableFields() {
        return List.of("vendorNo", "name", "country");
    }

    @Override
    protected String statusField() {
        return "status";
    }

    @Override
    protected void applyDeactivate(VendorRegister entity) {
        entity.setStatus("INACTIVE");
    }
}
