package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.Optional;

public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    Iterable<T> findAll();

    Iterable<T> findByFilter(Filter<T> field);

    void deleteById(ID id);
}
