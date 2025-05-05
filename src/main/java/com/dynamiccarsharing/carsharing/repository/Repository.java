package com.dynamiccarsharing.carsharing.repository;

import java.util.Map;

public interface Repository<T> {
    void save(T entity);

    T findById(Long id);

    T findByField(String fieldValue);

    void update(T entity);

    void delete(Long id);

    Map<Long, T> findAll();
}
