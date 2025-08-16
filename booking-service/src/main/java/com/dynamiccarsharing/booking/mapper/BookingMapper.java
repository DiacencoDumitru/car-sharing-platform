package com.dynamiccarsharing.booking.mapper;

import com.dynamiccarsharing.contracts.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.booking.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {
    BookingDto toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    Booking toEntity(BookingCreateRequestDto dto);

    default Booking fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Booking.builder().id(id).build();
    }
}