package com.loopers.infrastructure;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.CustomCrudRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class InMemoryCrudRepository<T extends BaseEntity> implements CustomCrudRepository<T> {

    protected final AtomicLong idGenerator = new AtomicLong(1L);
    protected final ConcurrentMap<Long, T> map = new ConcurrentHashMap<>();

    @Override
    public long count() {
        return map.size();
    }

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
            map.put(entity.getId(), entity);
            return entity;
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
        return map.values().stream().toList();
    }

    @Override
    public void deleteById(Long id) {
        T entity = map.get(id);
        entity.delete();
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }
}
