package com.dynamiccarsharing.booking.mapper;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {BookingMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentDto toDto(Payment entity);
}