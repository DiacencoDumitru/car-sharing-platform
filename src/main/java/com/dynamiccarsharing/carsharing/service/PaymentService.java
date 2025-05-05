package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;

import java.util.Map;

public class PaymentService {

    private final PaymentRepository paymentRepository = new PaymentRepository();

    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    public Payment findPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment findPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByField(String.valueOf(bookingId));
    }

    public void updatePayment(Payment payment) {
        paymentRepository.update(payment);
    }

    public void deletePayment(Long id) {
        paymentRepository.delete(id);
    }

    public Map<Long, Payment> findAllPayments() {
        return paymentRepository.findAll();
    }

    public Map<Long, Payment> findPaymentsByFilter(String field, String value) {
        return paymentRepository.findByFilter(field, value);
    }
}