package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface ContactInfoJpaRepository extends JpaRepository<ContactInfo, Long>, JpaSpecificationExecutor<ContactInfo> {

    Optional<ContactInfo> findByEmail(String email);

    List<ContactInfo> findByPhoneNumber(String phoneNumber);

    List<ContactInfo> findByFirstName(String firstName);

    List<ContactInfo> findByFirstNameIgnoreCase(String firstName);

    List<ContactInfo> findByLastName(String lastName);
}