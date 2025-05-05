package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingRepository implements Repository<Booking> {

    private final Map<Long, Booking> bookingsById = new HashMap<>();
    private final Map<Long, Booking> bookingsByRenterId = new HashMap<>();

    @Override
    public void save(Booking booking) {
        bookingsById.put(booking.getId(), booking);
        bookingsByRenterId.put(booking.getRenterId(), booking);
    }

    @Override
    public Booking findById(Long id) {
        return bookingsById.get(id);
    }

    @Override
    public Booking findByField(String fieldValue) {
        try {
            Long renterId = Long.parseLong(fieldValue);
            return bookingsByRenterId.get(renterId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void update(Booking booking) {
        if (bookingsById.containsKey(booking.getId())) {
            bookingsByRenterId.remove(bookingsById.get(booking.getId()).getRenterId());
            bookingsById.put(booking.getId(), booking);
            bookingsByRenterId.put(booking.getRenterId(), booking);
        }
    }

    @Override
    public void delete(Long id) {
        Booking booking = bookingsById.get(id);
        bookingsById.remove(id);
        bookingsByRenterId.remove(booking.getRenterId());
    }

    @Override
    public Map<Long, Booking> findAll() {
        return new HashMap<>(bookingsById);
    }

    public Map<Long, Booking> findByFilter(String field, String value) {
        return bookingsById.entrySet().stream()
                .filter(entry -> {
                    Booking booking = entry.getValue();
                    return (field.equals("status") && booking.getStatus().equals(value)) ||
                            (field.equals("disputeStatus") && booking.getDisputeStatus() != null && booking.getDisputeStatus().equals(value));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
