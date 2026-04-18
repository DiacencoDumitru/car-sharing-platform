package com.dynamiccarsharing.user.mapper;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {ContactInfoMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "instanceId", ignore = true)
    UserDto toDto(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(source = "role", target = "role")
    @Mapping(target = "reviewsOfUser", ignore = true)
    @Mapping(target = "reviewsByUser", ignore = true)
    User toEntity(UserCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contactInfo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "reviewsOfUser", ignore = true)
    @Mapping(target = "reviewsByUser", ignore = true)
    void updateFromDto(UserStatusUpdateRequestDto dto, @MappingTarget User user);

    default User fromId(Long id) {
        if (id == null) {
            return null;
        }
        return User.builder().id(id).build();
    }
}