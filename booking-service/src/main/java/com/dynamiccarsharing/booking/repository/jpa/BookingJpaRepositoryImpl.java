package com.dynamiccarsharing.booking.repository.jpa;

import lombok.RequiredArgsConstructor;
import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.specification.BookingSpecification;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDateTime;
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
                bookingFilter.getCarIds(),
                bookingFilter.getStatus()
        ));
    }

    @Override
    public Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        Specification<Booking> spec = BookingSpecification.withCriteria(
                criteria.getRenterId(),
                criteria.getCarId(),
                criteria.getCarIds(),
                criteria.getStatus()
        );
        return bookingJpaRepository.findAll(spec, pageable);
    }

    @Override
    public boolean hasOverlappingBooking(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TransactionStatus> active = List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED);
        return !bookingJpaRepository.findOverlapping(carId, active, startTime, endTime).isEmpty();
    }

    @Override
    public List<Booking> findOverlappingBookings(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TransactionStatus> active = List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED);
        return bookingJpaRepository.findOverlapping(carId, active, startTime, endTime);
    }
}