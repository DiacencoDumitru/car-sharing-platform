package com.dynamiccarsharing.user.repository;

import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ContactInfoRepository extends Repository<ContactInfo, Long> {

    Optional<ContactInfo> findByEmail(String email);

    List<ContactInfo> findByFilter(Filter<ContactInfo> filter) throws SQLException;
}