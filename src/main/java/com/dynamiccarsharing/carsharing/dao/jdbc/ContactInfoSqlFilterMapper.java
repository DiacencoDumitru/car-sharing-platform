package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class ContactInfoSqlFilterMapper implements SqlFilterMapper<ContactInfo, Filter<ContactInfo>> {

    @Override
    public SqlFilter toSqlFilter(Filter<ContactInfo> contactInfoFilter) {
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
        List<Object> params = new ArrayList<>();

        if (filter.getEmail() != null) {
            params.add(filter.getEmail());
        }
        if (filter.getPhoneNumber() != null) {
            params.add(filter.getPhoneNumber());
        }
        if (filter.getFirstName() != null) {
            params.add(filter.getFirstName());
        }
        if (filter.getLastName() != null) {
            params.add(filter.getLastName());
        }

        return params;
    }
}