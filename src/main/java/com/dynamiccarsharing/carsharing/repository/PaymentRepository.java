package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Payment;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaymentRepository implements Repository<Payment> {
    private final Map<Long, Payment> paymentsById = new HashMap<>();
    private final Map<Long, Payment> paymentsByBookingId = new HashMap<>();

    @Override
    public void save(Payment payment) {
        paymentsById.put(payment.getId(), payment);
        paymentsByBookingId.put(payment.getBookingId(), payment);
    }

    @Override
    public Payment findById(Long id) {
        return paymentsById.get(id);
    }

    @Override
    public Payment findByField(String fieldValue) {
        try {
            Long bookingId = Long.parseLong(fieldValue);
            return paymentsByBookingId.get(bookingId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void update(Payment payment) {
        if (paymentsById.containsKey(payment.getId())) {
            paymentsByBookingId.remove(paymentsById.get(payment.getId()).getBookingId());
            paymentsById.put(payment.getId(), payment);
            paymentsByBookingId.put(payment.getBookingId(), payment);
        }
    }

    @Override
    public void delete(Long id) {
        Payment payment = paymentsById.get(id);
        paymentsById.remove(id);
        paymentsByBookingId.remove(payment.getBookingId());
    }

    @Override
    public Map<Long, Payment> findAll() {
        return new HashMap<>(paymentsById);
    }

    public Map<Long, Payment> findByFilter(String field, String value) {
        return paymentsById.entrySet().stream()
                .filter(entry -> {
                    Payment payment = entry.getValue();
                    return (field.equals("status") && payment.getStatus().equals(value)) ||
                            (field.equals("paymentMethod") && payment.getPaymentMethod().equals(value));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}