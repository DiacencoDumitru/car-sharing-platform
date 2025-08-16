package com.dynamiccarsharing.user.dao.jdbc;


import com.dynamiccarsharing.user.filter.ContactInfoFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ContactInfoSqlFilterMapper implements SqlFilterMapper<ContactInfo, Filter<ContactInfo>> {

    @Override
    public SqlFilter toSqlFilter(Filter<ContactInfo> contactInfoFilter) {
        if (contactInfoFilter == null) {
            return SqlFilter.empty();
        }

        if (contactInfoFilter instanceof ContactInfoFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(ContactInfoFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getEmail() != null) {
            sb.append(" AND ").append("email = ?");
        }
        if (filter.getPhoneNumber() != null) {
            sb.append(" AND ").append("phone_number = ?");
        }
        if (filter.getFirstName() != null) {
            sb.append(" AND ").append("first_name = ?");
        }
        if (filter.getLastName() != null) {
            sb.append(" AND ").append("last_name = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(ContactInfoFilter filter) {
        return Stream.<Object>of(filter.getEmail(), filter.getPhoneNumber(), filter.getFirstName(), filter.getLastName())
                .filter(Objects::nonNull)
                .toList();
    }
}