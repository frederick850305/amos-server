package com.neusoft.amos.common;

import jakarta.persistence.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 通用注册表服务：为同质的基础字典（makers / vendors / locations / units / ...）提供
 * 统一的 list(搜索+状态过滤) / get / create / update / save。
 *
 * <p>搜索与状态过滤基于实体字段名动态构建 Specification，因此子类的实体只需声明
 * 普通 JPA 字段即可，无需继承公共基类。</p>
 *
 * @param <T> 实体
 * @param <R> Repository（需同时支持 Specification 查询）
 */
public class RegisterService<T, R extends JpaRepository<T, Long> & JpaSpecificationExecutor<T>> {

    protected final R repository;

    public RegisterService(R repository) {
        this.repository = repository;
    }

    /** 由 q（模糊搜索）+ status（状态/active 过滤）构造查询级 Specification。 */
    public Specification<T> buildSpec(String q, String status, List<String> searchFields, String statusField) {
        Specification<T> spec = Specification.where(null);
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Predicate p = cb.disjunction();
                for (String f : searchFields) {
                    p = cb.or(p, cb.like(cb.lower(root.get(f).as(String.class)), like));
                }
                return p;
            });
        }
        if (status != null && !status.isBlank() && statusField != null) {
            final String s = status;
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Path<?> path = root.get(statusField);
                if (path.getJavaType() == Boolean.class || path.getJavaType() == boolean.class) {
                    return cb.equal(path, Boolean.parseBoolean(s));
                }
                return cb.equal(path.as(String.class), s);
            });
        }
        return spec;
    }

    /** 全量（不分页）查询：返回 List，前端通用管理窗口默认走此路径（向后兼容）。 */
    public List<T> findAll(Specification<T> spec) {
        return repository.findAll(spec);
    }

    /** 分页查询：返回 Spring Data Page 信封（content / totalElements / totalPages ...）。 */
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    /** @deprecated 保留以兼容旧调用；新代码请直接用 {@link #findAll(Specification)}。 */
    @Deprecated
    public List<T> search(String q, String status, List<String> searchFields, String statusField) {
        return findAll(buildSpec(q, status, searchFields, statusField));
    }

    public T get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "not found: " + id));
    }

    public T create(T entity) {
        return repository.save(entity);
    }

    public T update(Long id, T entity) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("not found: " + id);
        }
        // 以路径 id 为准，覆盖 body 中的 id，保证 PUT /{id} 语义正确
        setId(entity, id);
        return repository.save(entity);
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    /** 将主键写回实体（泛型无法静态感知 @Id，故用反射注入）。 */
    private void setId(T entity, Long id) {
        try {
            Field idField = findIdField(entity.getClass());
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("无法为 " + entity.getClass().getSimpleName() + " 设置主键", e);
        }
    }

    private Field findIdField(Class<?> type) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchFieldException("未找到 @Id 字段: " + type.getName());
    }
}
