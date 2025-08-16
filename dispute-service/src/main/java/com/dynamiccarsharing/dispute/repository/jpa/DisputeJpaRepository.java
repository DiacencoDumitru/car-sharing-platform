package com.dynamiccarsharing.dispute.repository.jpa;

import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.dispute.filter.DisputeFilter;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.dispute.repository.DisputeRepository;
import com.dynamiccarsharing.dispute.specification.DisputeSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface DisputeJpaRepository extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute>, DisputeRepository {

    @Override
    Optional<Dispute> findByBookingId(Long bookingId);

    @Override
    List<Dispute> findByStatus(DisputeStatus status);

    @Override
    default List<Dispute> findByFilter(Filter<Dispute> filter) throws SQLException {
        if (!(filter instanceof DisputeFilter disputeFilter)) {
            throw new ValidationException("Filter must be an instance of DisputeFilter for JPA search.");
        }
        return findAll(DisputeSpecification.withCriteria(
                disputeFilter.getBookingId(),
                disputeFilter.getStatus()
        ));
    }
}