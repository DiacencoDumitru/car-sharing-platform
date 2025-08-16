package com.dynamiccarsharing.user.mapper;

import com.dynamiccarsharing.contracts.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.contracts.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.model.ContactInfo;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactInfoMapper {

    ContactInfoDto toDto(ContactInfo entity);

    @Mapping(target = "id", ignore = true)
    ContactInfo toEntity(ContactInfoCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(ContactInfoUpdateRequestDto dto, @MappingTarget ContactInfo entity);
}