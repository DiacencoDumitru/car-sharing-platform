package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.TransactionFilter;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
import com.dynamiccarsharing.booking.specification.TransactionSpecification;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
@Profile("jpa")
public interface TransactionJpaRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>, TransactionRepository {

    @Override
    List<Transaction> findByStatus(TransactionStatus status);

    @Override
    List<Transaction> findByBookingId(Long bookingId);

    @Override
    default List<Transaction> findByFilter(Filter<Transaction> filter) throws SQLException {
        if (!(filter instanceof TransactionFilter transactionFilter)) {
            throw new ValidationException("Filter must be an instance of TransactionFilter for JPA search.");
        }
        return findAll(TransactionSpecification.withCriteria(
                transactionFilter.getBookingId(),
                transactionFilter.getStatus(),
                transactionFilter.getPaymentMethod()
        ));
    }
}