package com.loopers.infrastructure;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.CustomCrudRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCrudRepository<T extends BaseEntity> implements CustomCrudRepository<T> {

    protected final AtomicLong idGenerator = new AtomicLong(1L);
    protected final Map<Long, T> map = new ConcurrentHashMap<>();

    @Override
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        if (entity.getId() == null || entity.getId() == 0L) {
            Long newId = idGenerator.getAndIncrement();
            T savedEntity = injectId(entity, newId);
            map.put(newId, savedEntity);
            return savedEntity;
        } else {
            T existingEntity = map.putIfAbsent(entity.getId(), entity);
            return existingEntity == null ? entity : existingEntity;
        }
    }

    private T injectId(T entity, Long id) {
        try {
            // BaseEntity의 id 필드에 리플렉션으로 값 주입
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("ID 주입 실패", e);
        }
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public List<T> findAllById(List<Long> ids) {
        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<T> findAll() {
        return map.values().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        T entity = map.get(id);
        entity.delete();
    }
}
