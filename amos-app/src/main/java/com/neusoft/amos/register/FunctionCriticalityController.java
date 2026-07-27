package com.neusoft.amos.register;

import com.neusoft.amos.common.RegisterController;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.util.List;

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

    /**
     * Function Criticality 无外键引用，删除应真正从库移除记录（而非软删置 active=false）。
     * 覆盖基类软删实现。列表默认不过滤 active，故软删后记录仍残留在界面上、仅变灰，
     * 与用户“点击 Delete 即删除”的预期不符，因此此处走物理删除。
     */
    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** 手册要求按 active 布尔过滤；查询级叠加，保证分页正确。 */
    @Override
    protected Specification<FunctionCriticality> applyExtraSpec(Specification<FunctionCriticality> spec,
                                                               Long installation,
                                                               Long parentId,
                                                               Boolean active) {
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        return spec;
    }
}
