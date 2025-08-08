package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.specification.BookingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("jpa")
public class BookingJpaRepositoryImpl implements BookingRepository {

    private final InternalBookingJpaRepository bookingJpaRepository;

    @Override
    public List<Booking> findByRenterId(Long renterId) {
        return bookingJpaRepository.findByRenterId(renterId);
    }

    @Override
    public Booking save(Booking entity) {
        return bookingJpaRepository.save(entity);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingJpaRepository.findById(id);
    }

    @Override
    public List<Booking> findAll() {
        return bookingJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        bookingJpaRepository.deleteById(id);
    }

    @Override
    public List<Booking> findByFilter(Filter<Booking> filter) throws SQLException {
        if (!(filter instanceof BookingFilter bookingFilter)) {
            throw new ValidationException("Filter must be an instance of BookingFilter for JPA search.");
        }
        return bookingJpaRepository.findAll(BookingSpecification.withCriteria(
                bookingFilter.getRenterId(),
                bookingFilter.getCarId(),
                bookingFilter.getStatus()
        ));
    }

    @Override
    public Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        Specification<Booking> spec = BookingSpecification.withCriteria(
                criteria.getRenterId(),
                criteria.getCarId(),
                criteria.getStatus()
        );
        return bookingJpaRepository.findAll(spec, pageable);
    }
}