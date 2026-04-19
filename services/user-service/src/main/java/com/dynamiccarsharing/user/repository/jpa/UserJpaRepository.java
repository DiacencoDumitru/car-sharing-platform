package com.dynamiccarsharing.user.repository.jpa;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.user.filter.UserFilter;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.user.specification.UserSpecification;
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
    Optional<User> findByReferralCode(String referralCode);

    @Override
    boolean existsByReferralCode(String referralCode);

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