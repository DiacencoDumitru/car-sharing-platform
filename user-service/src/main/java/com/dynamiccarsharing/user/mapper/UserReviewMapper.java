package com.dynamiccarsharing.user.mapper;

import com.dynamiccarsharing.contracts.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserReviewDto;
import com.dynamiccarsharing.contracts.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.user.model.UserReview;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface UserReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    UserReviewDto toDto(UserReview entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userId", target = "user")
    @Mapping(source = "dto.reviewerId", target = "reviewer")
    @Mapping(source = "dto.comment", target = "comment")
    UserReview toEntity(UserReviewCreateRequestDto dto, Long userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UserReviewUpdateRequestDto dto, @MappingTarget UserReview entity);

    default UserReview fromId(Long userReviewId) {
        if (userReviewId == null) {
            return null;
        }
        return UserReview.builder().id(userReviewId).build();
    }
}