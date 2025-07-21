package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.filter.Filter;

public interface SqlFilterMapper<T, F extends Filter<T>> {

    SqlFilter toSqlFilter(F filter);
}
