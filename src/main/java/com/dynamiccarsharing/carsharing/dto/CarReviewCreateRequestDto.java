package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarReviewCreateRequestDto {
<<<<<<< HEAD

    private Long carId;

=======
>>>>>>> fix/controller-mvc-tests
    @NotNull
    private Long reviewerId;
    @NotBlank
    private String comment;
}