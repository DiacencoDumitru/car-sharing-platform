package com.dynamiccarsharing.booking.mapper;

import com.dynamiccarsharing.contracts.dto.PaymentDto;
import com.dynamiccarsharing.contracts.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {BookingMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentDto toDto(Payment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "bookingId", target = "booking")
    Payment toEntity(PaymentRequestDto dto);
}