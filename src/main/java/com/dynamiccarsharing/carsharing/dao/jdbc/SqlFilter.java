package com.dynamiccarsharing.carsharing.dao.jdbc;

import java.util.Collections;
import java.util.List;

public record SqlFilter(String filterQuery, List<Object> parameters) {

    public static SqlFilter empty() {
        return new SqlFilter("", Collections.emptyList());
    }

    public Object[] parametersArray() {
        return parameters.toArray();
    }
}
