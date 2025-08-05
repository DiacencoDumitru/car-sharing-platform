package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserFilter;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.specification.UserSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>, UserRepository {

    @Override
    List<User> findByRole(UserRole role);

    @Override
    List<User> findByStatus(UserStatus status);

    @Override
    Optional<User> findByContactInfoEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"cars"})
    Optional<User> findWithCarsById(Long id);

    @Override
    default List<User> findByFilter(Filter<User> filter) throws SQLException {
        if (!(filter instanceof UserFilter userFilter)) {
            throw new ValidationException("Filter must be an instance of UserFilter for JPA search.");
        }
        return findAll(UserSpecification.withCriteria(
                userFilter.getEmail(),
                userFilter.getRole(),
                userFilter.getStatus()
        ));
    }
}