package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.BookingSummaryResponseDto;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.service.interfaces.BookingSummaryService;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.booking.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingSummaryServiceImpl implements BookingSummaryService {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;

    @Override
    public Optional<BookingSummaryResponseDto> findByBookingId(Long bookingId) {
        return bookingService.findById(bookingId).map(booking -> {
            BookingSummaryResponseDto dto = new BookingSummaryResponseDto();
            dto.setBooking(booking);
            dto.setPayment(paymentService.findByBookingId(bookingId).orElse(null));
            dto.setTransactions(transactionService.findTransactionsByBookingId(bookingId));
            return dto;
        });
    }
}
