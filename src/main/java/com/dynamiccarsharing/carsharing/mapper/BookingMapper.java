package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CarMapper.class, LocationMapper.class})
public interface BookingMapper {

    @Mapping(source = "renter.id", target = "renterId")
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "pickupLocation.id", target = "pickupLocationId")
    BookingDto toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "renterId", target = "renter")
    @Mapping(source = "carId", target = "car")
    @Mapping(source = "pickupLocationId", target = "pickupLocation")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "disputeDescription", ignore = true)
    @Mapping(target = "disputeStatus", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "dispute", ignore = true)
    Booking toEntity(BookingCreateRequestDto dto);

    default Booking fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Booking.builder().id(id).build();
    }
}