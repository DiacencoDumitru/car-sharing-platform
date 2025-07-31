package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.ContactInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ContactInfoRepository extends Repository<ContactInfo, Long> {

    Optional<ContactInfo> findByEmail(String email);

    List<ContactInfo> findByFilter(Filter<ContactInfo> filter) throws SQLException;
}