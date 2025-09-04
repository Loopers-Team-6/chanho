package com.loopers.infrastructure;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.CustomCrudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractRepositoryImpl<T extends BaseEntity, R extends JpaRepository<T, Long>> implements CustomCrudRepository<T> {
    protected final R jpaRepository;

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public T save(T entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<T> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<T> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids);
    }

    @Override
    public List<T> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.findById(id)
                .ifPresent(entity -> {
                    entity.delete();
                    save(entity);
                });
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("Entities cannot be null or empty");
        }

        return jpaRepository.saveAll(entities);
    }

    @Override
    public T getReferenceById(Long id) {
        return jpaRepository.getReferenceById(id);
    }
}
