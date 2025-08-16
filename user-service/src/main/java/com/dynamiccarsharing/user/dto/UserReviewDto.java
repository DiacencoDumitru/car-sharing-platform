package com.dynamiccarsharing.contracts.dto;

import lombok.Data;

@Data
public class UserReviewDto {
    private Long id;
    private Long userId;
    private Long reviewerId;
    private String comment;
}