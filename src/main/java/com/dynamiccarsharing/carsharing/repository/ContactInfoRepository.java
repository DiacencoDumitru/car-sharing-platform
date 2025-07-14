package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, UUID>, JpaSpecificationExecutor<ContactInfo> {

    Optional<ContactInfo> findByEmail(String email);

    List<ContactInfo> findByPhoneNumber(String phoneNumber);

    List<ContactInfo> findByFirstName(String firstName);

    List<ContactInfo> findByFirstNameIgnoreCase(String firstName);

    List<ContactInfo> findByLastName(String lastName);
}
