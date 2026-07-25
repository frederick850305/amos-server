package com.neusoft.amos.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用注册表控制器：为同质的基础字典提供统一的 REST 端点。
 *
 * <p>子类只需声明三件事：
 * <ol>
 *   <li>{@link #searchableFields()} —— 参与 {@code ?q=} 模糊搜索的字段名；</li>
 *   <li>{@link #statusField()} —— 状态过滤字段名（{@code status} 字符串或 {@code active} 布尔）；</li>
 *   <li>{@link #applyDeactivate(T)} —— 软删时将记录置为失效（INACTIVE / active=false）。</li>
 * </ol>
 * 重复编码由唯一约束触发 {@code DataIntegrityViolationException}，由 GlobalExceptionHandler 统一返回 409。</p>
 *
 * @param <T> 实体
 * @param <R> Repository
 */
@RestController
public abstract class RegisterController<T, R extends JpaRepository<T, Long> & JpaSpecificationExecutor<T>> {

    protected final RegisterService<T, R> service;

    protected RegisterController(R repository) {
        this.service = new RegisterService<>(repository);
    }

    /** 参与 {@code ?q=} 模糊搜索的实体字段名（应为字符串类型）。 */
    protected abstract List<String> searchableFields();

    /** 状态过滤字段名；为 null 表示不支持状态过滤。 */
    protected abstract String statusField();

    /** 软删：把记录置为失效（INACTIVE / active=false）。遵循 data-model 不物理删除原则。 */
    protected abstract void applyDeactivate(T entity);

    @GetMapping
    public List<T> list(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Long installation,
                        @RequestParam(required = false) Long parentId,
                        @RequestParam(required = false) Boolean active) {
        List<T> result = service.search(q, status, searchableFields(), statusField());
        return applyExtraFilters(result, installation, parentId, active);
    }

    /**
     * 子类可重写以叠加 register 专属过滤（如 location 的 installation/parentId、
     * function_criticality 的 active）。默认原样返回。参数为 null 时忽略。
     */
    protected List<T> applyExtraFilters(List<T> result,
                                        Long installation,
                                        Long parentId,
                                        Boolean active) {
        return result;
    }

    @GetMapping("/{id}")
    public T get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T create(@RequestBody T entity) {
        return service.create(entity);
    }

    @PutMapping("/{id}")
    public T update(@PathVariable Long id, @RequestBody T entity) {
        return service.update(id, entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        T entity = service.get(id);
        applyDeactivate(entity);
        service.save(entity);
    }
}
