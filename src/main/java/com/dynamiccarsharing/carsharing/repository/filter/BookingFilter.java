package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import lombok.Getter;

@Getter
public class BookingFilter implements Filter<Booking> {
    private final Long renterId;
    private final Long carId;
    private final TransactionStatus status;

    private BookingFilter(Long renterId, Long carId, TransactionStatus status) {
        this.renterId = renterId;
        this.carId = carId;
        this.status = status;
    }

    public static BookingFilter of(Long renterId, Long carId, TransactionStatus status) {
        return new BookingFilter(renterId, carId, status);
    }

    public static BookingFilter ofRenterId(Long renterId) {
        return new BookingFilter(renterId, null, null);
    }

    public static BookingFilter ofCarId(Long carId) {
        return new BookingFilter(null, carId, null);
    }

    public static BookingFilter ofStatus(TransactionStatus status) {
        return new BookingFilter(null, null, status);
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