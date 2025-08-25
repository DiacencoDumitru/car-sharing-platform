package com.dynamiccarsharing.booking.mapper;

import com.dynamiccarsharing.booking.dto.TransactionDto;
import com.dynamiccarsharing.booking.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    TransactionDto toDto(Transaction entity);
}