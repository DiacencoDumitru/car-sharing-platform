package com.dynamiccarsharing.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookingMessageCreateRequestDto {

    @NotBlank(message = "Message body cannot be blank.")
    @Size(max = 2000, message = "Message body must be at most 2000 characters.")
    private String body;
}
