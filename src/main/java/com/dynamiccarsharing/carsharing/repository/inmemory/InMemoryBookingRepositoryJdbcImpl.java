package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

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
        List<Booking> filteredBookings = bookingMap.values().stream().filter(filter::test).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredBookings.size());

        List<Booking> pageContent = (start <= end) ? filteredBookings.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filteredBookings.size());
    }

    @Override
    public List<Booking> findByRenterId(Long renterId) {
        return bookingMap.values().stream()
                .filter(booking -> booking.getRenter() != null && booking.getRenter().getId().equals(renterId))
                .toList();
    }
}