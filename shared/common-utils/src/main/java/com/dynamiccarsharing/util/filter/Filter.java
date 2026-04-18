package com.dynamiccarsharing.util.filter;

public interface Filter<T> {
    boolean test(T entity);
}