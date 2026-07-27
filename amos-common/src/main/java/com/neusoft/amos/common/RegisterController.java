package com.neusoft.amos.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    /**
     * 列表查询。支持可选分页：传 {@code page}/{@code size}（及可选 {@code sort=field,dir}）
     * 时返回 Spring Data {@link Page} 信封（content / totalElements / totalPages …）；
     * 不传分页参数时返回 {@link List}（前端通用管理窗口默认路径，向后兼容）。
     *
     * <p>q / status / installation / parentId / active 全部在查询级（Specification）完成，
     * 因此分页计数始终基于过滤后的真实结果集。</p>
     */
    @GetMapping
    public Object list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long installation,
                       @RequestParam(required = false) Long parentId,
                       @RequestParam(required = false) Boolean active,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) String sort) {
        Specification<T> spec = service.buildSpec(q, status, searchableFields(), statusField());
        spec = applyExtraSpec(spec, installation, parentId, active);
        if (page != null || size != null) {
            Pageable pageable = buildPageable(page, size, sort);
            return service.findAll(spec, pageable);
        }
        return service.findAll(spec);
    }

    /** 根据可选分页参数构造 Pageable；page/size 任一出现即启用分页，缺省 page=0、size=20。 */
    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            Sort.Direction dir = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(p, s, Sort.by(dir, parts[0]));
        }
        return PageRequest.of(p, s);
    }

    /**
     * 子类可重写以叠加 register 专属过滤（如 location 的 installation/parentId、
     * function_criticality 的 active）。必须在查询级（Specification）完成，以保证分页正确。
     * 默认原样返回。参数为 null 时忽略。
     */
    protected Specification<T> applyExtraSpec(Specification<T> spec,
                                              Long installation,
                                              Long parentId,
                                              Boolean active) {
        return spec;
    }

    /**
     * @deprecated 改为查询级的 {@link #applyExtraSpec}（否则分页计数会错位）。
     * 默认实现保留以兼容旧子类，但基类不再调用。
     */
    @Deprecated
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
