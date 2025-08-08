package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingRepository extends Repository<Booking, Long> {

    List<Booking> findByRenterId(Long renterId);

    Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable);
}