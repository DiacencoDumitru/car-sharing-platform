package com.dynamiccarsharing.carsharing.repository.filter;

public interface Filter<T> {
    boolean test(T entity);
}
