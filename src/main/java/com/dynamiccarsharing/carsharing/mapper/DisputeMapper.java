package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
import com.dynamiccarsharing.carsharing.model.Dispute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {BookingMapper.class, UserMapper.class}, imports = LocalDateTime.class)
public interface DisputeMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "creationUser.id", target = "creationUserId")
    DisputeDto toDto(Dispute entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(source = "bookingId", target = "booking")
    @Mapping(source = "creationUserId", target = "creationUser")
    @Mapping(source = "dto.description", target = "description")
    Dispute toEntity(DisputeCreateRequestDto dto, Long bookingId, Long creationUserId);
}