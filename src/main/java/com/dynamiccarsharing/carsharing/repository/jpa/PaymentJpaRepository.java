package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.specification.PaymentSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment>, PaymentRepository {

    @Override
    Optional<Payment> findByBookingId(Long bookingId);

    @Override
    List<Payment> findByStatus(TransactionStatus status);

    @Override
    default List<Payment> findByFilter(Filter<Payment> filter) throws SQLException {
        if (!(filter instanceof PaymentFilter paymentFilter)) {
            throw new ValidationException("Filter must be an instance of PaymentFilter for JPA search.");
        }
        return findAll(PaymentSpecification.withCriteria(
                paymentFilter.getBookingId(),
                paymentFilter.getAmount(),
                paymentFilter.getStatus(),
                paymentFilter.getPaymentMethod()
        ));
    }
}