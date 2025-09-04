package com.loopers.domain;

import java.util.List;
import java.util.Optional;

public interface CustomCrudRepository<T extends BaseEntity> {

    long count();

    T save(T entity);

    Optional<T> findById(Long id);

    List<T> findAllById(List<Long> ids);

    List<T> findAll();

    void deleteById(Long id);

    List<T> saveAll(List<T> entities);

    T getReferenceById(Long id);
}
