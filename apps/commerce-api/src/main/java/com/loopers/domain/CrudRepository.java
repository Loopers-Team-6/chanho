package com.loopers.domain;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {

    T save(T entity);

    Optional<T> findById(Long id);

    List<T> findByIdIn(List<Long> ids);

    List<T> findAll();

    void deleteById(Long id);

}
