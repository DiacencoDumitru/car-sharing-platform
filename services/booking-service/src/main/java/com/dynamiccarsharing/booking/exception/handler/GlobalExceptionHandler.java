package com.dynamiccarsharing.booking.exception.handler;

import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.util.web.AbstractGlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFoundException(BookingNotFoundException ex) {
        ProblemDetail problemDetail = problem(NOT_FOUND, "Booking Not Found", ex.getMessage(), "/errors/booking-not-found");
        problemDetail.setProperty("service", "booking-service");
        return problemDetail;
    }

    @ExceptionHandler(InvalidBookingStatusException.class)
    public ProblemDetail handleInvalidBookingStatusException(InvalidBookingStatusException ex) {
        return problem(CONFLICT, "Invalid Booking Status", ex.getMessage(), "/errors/booking-status-conflict");
    }
}