package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.booking.filter.PaymentFilter;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.specification.PaymentSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
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