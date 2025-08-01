package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ContactInfoMapper.class})
public interface UserMapper {

    UserDto toDto(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "cars", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "disputes", ignore = true)
    @Mapping(target = "reviewsOfUser", ignore = true)
    @Mapping(target = "reviewsByUser", ignore = true)
    @Mapping(target = "carReviewsByUser", ignore = true)
    User toEntity(UserCreateRequestDto dto);
}