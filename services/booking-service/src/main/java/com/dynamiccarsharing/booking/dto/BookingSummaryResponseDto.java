package com.dynamiccarsharing.booking.dto;

import com.dynamiccarsharing.contracts.dto.BookingDto;
import lombok.Data;

import java.util.List;

@Data
public class BookingSummaryResponseDto {
    private BookingDto booking;
    private PaymentDto payment;
    private List<TransactionDto> transactions;
}
