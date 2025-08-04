package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.specification.DisputeSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface DisputeJpaRepository extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute>, com.dynamiccarsharing.carsharing.repository.DisputeRepository {

    @Override
    Optional<Dispute> findByBookingId(Long bookingId);

    @Override
    List<Dispute> findByStatus(DisputeStatus status);

    @Override
    default List<Dispute> findByFilter(Filter<Dispute> filter) throws SQLException {
        if (!(filter instanceof DisputeFilter disputeFilter)) {
            throw new IllegalArgumentException("Filter must be an instance of DisputeFilter for JPA search.");
        }
        return findAll(DisputeSpecification.withCriteria(
                disputeFilter.getBookingId(),
                disputeFilter.getStatus()
        ));
    }
}