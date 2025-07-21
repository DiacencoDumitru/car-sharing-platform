package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    Optional<User> findByContactInfoEmail(String email);

    @EntityGraph(attributePaths = {"cars"})
    Optional<User> findWithCarsById(Long id);
}