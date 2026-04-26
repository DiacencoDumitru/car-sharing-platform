package com.dynamiccarsharing.booking.exception.handler;

import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.util.web.AbstractGlobalExceptionHandler;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @Override
    protected String serviceName() {
        return "booking-service";
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFoundException(BookingNotFoundException ex) {
        return problem(NOT_FOUND, "Booking Not Found", ex.getMessage(), "/errors/booking-not-found");
    }

    @ExceptionHandler(InvalidBookingStatusException.class)
    public ProblemDetail handleInvalidBookingStatusException(InvalidBookingStatusException ex) {
        return problem(CONFLICT, "Invalid Booking Status", ex.getMessage(), "/errors/booking-status-conflict");
    }
}