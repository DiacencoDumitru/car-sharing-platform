package com.dynamiccarsharing.user.mapper;

import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.user.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.model.ContactInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactInfoMapper {

    ContactInfoDto toDto(ContactInfo entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    ContactInfo toEntity(ContactInfoCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateFromDto(ContactInfoUpdateRequestDto dto, @MappingTarget ContactInfo entity);
}