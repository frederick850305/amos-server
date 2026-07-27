package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /** 在通用 q/status 搜索之上叠加 installation / parentId 过滤（查询级，保证分页正确）。 */
    @Override
    protected Specification<LocationRegister> applyExtraSpec(Specification<LocationRegister> spec,
                                                            Long installation,
                                                            Long parentId,
                                                            Boolean active) {
        if (installation != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("installationId"), installation));
        }
        if (parentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("parentLocationId"), parentId));
        }
        return spec;
    }
}
