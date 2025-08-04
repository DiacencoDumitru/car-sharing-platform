<<<<<<< HEAD
=======
// in package com.dynamiccarsharing.carsharing.dto
>>>>>>> fix/controller-mvc-tests
package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusUpdateRequestDto {
    @NotNull
    private TransactionStatus status;
}