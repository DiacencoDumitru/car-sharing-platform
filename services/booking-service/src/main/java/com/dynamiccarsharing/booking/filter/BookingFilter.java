package com.dynamiccarsharing.booking.filter;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class BookingFilter implements Filter<Booking> {
    private final Long renterId;
    private final Long carId;
    private final List<Long> carIds;
    private final TransactionStatus status;

    private BookingFilter(Long renterId, Long carId, List<Long> carIds, TransactionStatus status) {
        this.renterId = renterId;
        this.carId = carId;
        this.carIds = carIds;
        this.status = status;
    }

    public static BookingFilter of(Long renterId, Long carId, TransactionStatus status) {
        return new BookingFilter(renterId, carId, null, status);
    }

    public static BookingFilter of(Long renterId, Long carId, List<Long> carIds, TransactionStatus status) {
        return new BookingFilter(renterId, carId, carIds, status);
    }

    public static BookingFilter ofRenterId(Long renterId) {
        return new BookingFilter(renterId, null, null, null);
    }

    public static BookingFilter ofCarId(Long carId) {
        return new BookingFilter(null, carId, null, null);
    }

    public static BookingFilter ofStatus(TransactionStatus status) {
        return new BookingFilter(null, null, null, status);
    }

    @Override
    public boolean test(Booking booking) {
        boolean matches = true;
        if (renterId != null) matches &= Objects.equals(booking.getRenterId(), renterId);
        if (carIds != null && !carIds.isEmpty()) {
            matches &= carIds.contains(booking.getCarId());
        } else if (carId != null) {
            matches &= Objects.equals(booking.getCarId(), carId);
        }
        if (status != null) matches &= booking.getStatus() == status;
        return matches;
    }
}