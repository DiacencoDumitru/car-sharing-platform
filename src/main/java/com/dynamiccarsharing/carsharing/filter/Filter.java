package com.dynamiccarsharing.carsharing.filter;

public interface Filter<T> {
    boolean test(T entity);
}