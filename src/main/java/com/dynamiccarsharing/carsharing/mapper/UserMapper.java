package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {ContactInfoMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    UserDto toDto(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "role", constant = "RENTER")
    @Mapping(target = "cars", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "disputes", ignore = true)
    @Mapping(target = "reviewsOfUser", ignore = true)
    @Mapping(target = "reviewsByUser", ignore = true)
    @Mapping(target = "carReviewsByUser", ignore = true)
    User toEntity(UserCreateRequestDto dto);

    void updateFromDto(UserStatusUpdateRequestDto dto, @MappingTarget User user);

    default User fromId(Long id) {
        if (id == null) {
            return null;
        }
        return User.builder().id(id).build();
    }
}