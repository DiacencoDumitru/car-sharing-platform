package com.dynamiccarsharing.dispute.mapper;

import com.dynamiccarsharing.contracts.dto.DisputeDto;
import com.dynamiccarsharing.dispute.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.dispute.model.Dispute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface DisputeMapper {

    DisputeDto toDto(Dispute entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(source = "bookingId", target = "bookingId")
    @Mapping(source = "creationUserId", target = "creationUserId")
    @Mapping(source = "dto.description", target = "description")
    Dispute toEntity(DisputeCreateRequestDto dto, Long bookingId, Long creationUserId);
}