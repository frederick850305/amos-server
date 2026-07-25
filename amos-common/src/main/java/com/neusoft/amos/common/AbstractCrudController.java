package com.neusoft.amos.common;

import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 通用 CRUD 控制器：对应前端的 collectionService（collection/push/removeBy）。
 * 每个 dataKey 资源只需继承本类并标注 @RestController + @RequestMapping 即可，
 * 实现"加集合即加端点"，与 windowRegistry 元数据驱动模式一致。
 *
 * @param <T>  实体
 * @param <ID> 主键
 * @param <R>  Repository
 */
public abstract class AbstractCrudController<T, ID, R extends JpaRepository<T, ID>> {

    protected final R repository;

    protected AbstractCrudController(R repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<T> list() {
        return repository.findAll();
    }

    @PostMapping
    public T create(@RequestBody T entity) {
        return repository.save(entity);
    }

    @GetMapping("/{id}")
    public T get(@PathVariable ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }

    @PutMapping("/{id}")
    public T update(@PathVariable ID id, @RequestBody T entity) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("not found: " + id);
        }
        // 以路径 id 为准，覆盖 body 中的 id，保证 PUT /{id} 语义正确
        setId(entity, id);
        return repository.save(entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable ID id) {
        repository.deleteById(id);
    }

    /** 将主键写回实体（实体 id 由 @Id 标注，泛型无法静态感知，故用反射注入）。 */
    private void setId(T entity, ID id) {
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
