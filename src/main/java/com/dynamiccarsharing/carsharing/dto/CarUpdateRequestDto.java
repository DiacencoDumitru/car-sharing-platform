package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import jakarta.validation.constraints.Positive;
import lombok.Data;
<<<<<<< HEAD

=======
>>>>>>> fix/controller-mvc-tests
import java.math.BigDecimal;

@Data
public class CarUpdateRequestDto {
<<<<<<< HEAD
    private String registrationNumber;
    private String make;
    private String model;

=======
>>>>>>> fix/controller-mvc-tests
    private Long locationId;
    @Positive
    private BigDecimal price;
    private CarStatus status;
}