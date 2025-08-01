package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "renter.id", target = "renterId")
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "pickupLocation.id", target = "pickupLocationId")
    BookingDto toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "renterId", target = "renter.id")
    @Mapping(source = "carId", target = "car.id")
    @Mapping(source = "pickupLocationId", target = "pickupLocation.id")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "disputeDescription", ignore = true)
    @Mapping(target = "disputeStatus", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "dispute", ignore = true)
    Booking toEntity(BookingCreateRequestDto dto);
}