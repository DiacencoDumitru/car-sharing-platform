package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentDto toDto(Payment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(source = "bookingId", target = "booking")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(PaymentRequestDto dto, Long bookingId);

    default Booking map(Long bookingId) {
        if (bookingId == null) {
            return null;
        }
        return Booking.builder().id(bookingId).build();
    }
}