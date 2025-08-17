package com.dynamiccarsharing.user.dto;

import lombok.Data;

@Data
public class UserReviewDto {
    private Long id;
    private Long userId;
    private Long reviewerId;
    private String comment;
}