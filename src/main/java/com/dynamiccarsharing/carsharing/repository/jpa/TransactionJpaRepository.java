package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.TransactionFilter;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.dynamiccarsharing.carsharing.specification.TransactionSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Profile("jpa")
@Repository
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