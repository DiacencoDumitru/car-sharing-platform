package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    UserReviewDto toDto(UserReview entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userId", target = "user")
    @Mapping(source = "dto.reviewerId", target = "reviewer.id")
    @Mapping(source = "dto.comment", target = "comment")
    UserReview toEntity(UserReviewCreateRequestDto dto, Long userId);

    default User map(Long userId) {
        if (userId == null) {
            return null;
        }
        return User.builder().id(userId).build();
    }
}