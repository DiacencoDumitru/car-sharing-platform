package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactInfoMapper {

    ContactInfoDto toDto(ContactInfo entity);

    @Mapping(target = "id", ignore = true)
    ContactInfo toEntity(ContactInfoCreateRequestDto dto);

<<<<<<< HEAD
=======
    @Mapping(target = "id", ignore = true)
    ContactInfo toEntity(ContactInfoUpdateRequestDto dto);

>>>>>>> fix/controller-mvc-tests
    void updateFromDto(ContactInfoUpdateRequestDto dto, @MappingTarget ContactInfo entity);
}