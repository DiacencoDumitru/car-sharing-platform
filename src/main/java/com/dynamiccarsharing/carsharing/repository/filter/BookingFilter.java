package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;

public class BookingFilter implements Filter<Booking> {
    private Long renterId;
    private Long carId;
    private TransactionStatus status;

    public BookingFilter setRenterId(Long renterId) {
        this.renterId = renterId;
        return this;
    }

    public BookingFilter setCarId(Long carId) {
        this.carId = carId;
        return this;
    }

    public BookingFilter setStatus(TransactionStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean test(Booking booking) {
        boolean matches = true;
        if (renterId != null) matches &= booking.getRenterId().equals(renterId);
        if (carId != null) matches &= booking.getCarId().equals(carId);
        if (status != null) matches &= booking.getStatus() == status;
        return matches;
    }
}