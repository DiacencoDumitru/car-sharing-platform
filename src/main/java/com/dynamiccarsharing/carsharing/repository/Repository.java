package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.filter.Filter;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    Iterable<T> findAll();

    void deleteById(ID id);

    List<T> findByFilter(Filter<T> filter) throws SQLException;
}
