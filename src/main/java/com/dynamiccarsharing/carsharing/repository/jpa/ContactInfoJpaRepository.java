package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.specification.ContactInfoSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface ContactInfoJpaRepository extends JpaRepository<ContactInfo, Long>, JpaSpecificationExecutor<ContactInfo>, ContactInfoRepository {

    @Override
    Optional<ContactInfo> findByEmail(String email);

    List<ContactInfo> findByPhoneNumber(String phoneNumber);

    List<ContactInfo> findByFirstName(String firstName);

    List<ContactInfo> findByFirstNameIgnoreCase(String firstName);

    List<ContactInfo> findByLastName(String lastName);

    @Override
    default List<ContactInfo> findByFilter(Filter<ContactInfo> filter) throws SQLException {
        if (!(filter instanceof ContactInfoFilter contactInfoFilter)) {
            throw new IllegalArgumentException("Filter must be an instance of ContactInfoFilter for JPA search.");
        }
        return findAll(ContactInfoSpecification.withCriteria(
                contactInfoFilter.getFirstName(),
                contactInfoFilter.getLastName(),
                contactInfoFilter.getEmail()
        ));
    }
}