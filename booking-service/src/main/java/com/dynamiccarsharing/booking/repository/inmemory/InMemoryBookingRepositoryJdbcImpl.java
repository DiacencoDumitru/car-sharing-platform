package com.dynamiccarsharing.booking.repository.inmemory;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.filter.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryBookingRepositoryJdbcImpl implements BookingRepository {
    private final Map<Long, Booking> bookingMap = new HashMap<>();

    @Override
    public Booking save(Booking booking) {
        bookingMap.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookingMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        bookingMap.remove(id);
    }

    @Override
    public List<Booking> findByFilter(Filter<Booking> filter) {
        return bookingMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookingMap.values());
    }

    @Override
    public Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        Filter<Booking> filter = BookingFilter.of(criteria.getRenterId(), criteria.getCarId(), criteria.getStatus());
        List<Booking> filteredBookings = bookingMap.values().stream().filter(filter::test).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredBookings.size());

        List<Booking> pageContent = start <= end ? filteredBookings.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filteredBookings.size());
    }

    @Override
    public List<Booking> findByRenterId(Long renterId) {
        return bookingMap.values().stream()
                .filter(booking -> booking.getRenterId().equals(renterId))
                .toList();
    }

    @Override
    public boolean hasOverlappingBooking(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingMap.values().stream()
                .filter(b -> carId.equals(b.getCarId()) && (b.getStatus() == TransactionStatus.PENDING || b.getStatus() == TransactionStatus.APPROVED))
                .anyMatch(b -> b.getStartTime().isBefore(endTime) && b.getEndTime().isAfter(startTime));
    }

    @Override
    public List<Booking> findOverlappingBookings(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingMap.values().stream()
                .filter(b -> carId.equals(b.getCarId()) && (b.getStatus() == TransactionStatus.PENDING || b.getStatus() == TransactionStatus.APPROVED))
                .filter(b -> b.getStartTime().isBefore(endTime) && b.getEndTime().isAfter(startTime))
                .toList();
    }
}