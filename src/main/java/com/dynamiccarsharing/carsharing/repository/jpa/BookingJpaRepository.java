package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.specification.BookingSpecification;
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
public interface BookingJpaRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking>, BookingRepository {
    @EntityGraph(attributePaths = {"transactions"})
    Optional<Booking> findWithTransactionById(Long id);

    @Override
    List<Booking> findByRenterId(Long renterId);

    @Override
    default List<Booking> findByFilter(Filter<Booking> filter) throws SQLException {
        if (!(filter instanceof BookingFilter bookingFilter)) {
            throw new ValidationException("Filter must be an instance of BookingFilter for JPA search.");
        }
        return findAll(BookingSpecification.withCriteria(
                bookingFilter.getRenterId(),
                bookingFilter.getCarId(),
                bookingFilter.getStatus()
        ));
    }
}