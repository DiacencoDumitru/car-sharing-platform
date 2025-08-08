package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserReviewUpdateRequestDto {
    @NotBlank(message = "Comment cannot be blank.")
    private String comment;
}