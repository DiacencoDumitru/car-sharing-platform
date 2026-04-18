package com.dynamiccarsharing.util.dao.jdbc;


import com.dynamiccarsharing.util.filter.Filter;

public interface SqlFilterMapper<T, F extends Filter<T>> {

    SqlFilter toSqlFilter(F filter);
}
