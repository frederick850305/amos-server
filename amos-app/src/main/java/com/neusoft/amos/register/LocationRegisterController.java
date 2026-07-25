package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/register/locations")
public class LocationRegisterController extends RegisterController<LocationRegister, LocationRegisterRepository> {

    public LocationRegisterController(LocationRegisterRepository repository) {
        super(repository);
    }

    @Override
    protected List<String> searchableFields() {
        return List.of("code", "name", "locationType");
    }

    @Override
    protected String statusField() {
        return "status";
    }

    @Override
    protected void applyDeactivate(LocationRegister entity) {
        entity.setStatus("INACTIVE");
    }

    /** 在通用 q/status 搜索之上叠加 installation / parentId 过滤（手册要求）。 */
    @Override
    protected List<LocationRegister> applyExtraFilters(List<LocationRegister> result,
                                                       Long installation,
                                                       Long parentId,
                                                       Boolean active) {
        List<LocationRegister> all = result;
        if (installation != null) {
            all = all.stream().filter(l -> installation.equals(l.getInstallationId())).collect(Collectors.toList());
        }
        if (parentId != null) {
            all = all.stream().filter(l -> parentId.equals(l.getParentLocationId())).collect(Collectors.toList());
        }
        return all;
    }
}
