package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
public class Dispute {
    private final Long id;
    private final Long bookingId;
    private final Long creationUserId;
    private final String description;
    private final DisputeStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;

    public Dispute(Long id, Long bookingId, Long creationUserId, String description, DisputeStatus status, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        Validator.validateId(id, "ID");
        Validator.validateId(bookingId, "Booking ID");
        Validator.validateOptionalString(description, "Description");
        Validator.validateNonNull(status, "Status");
        Validator.validateNonNull(createdAt, "Created at");
        if (status == DisputeStatus.RESOLVED) {
            Validator.validateNonNull(resolvedAt, "Resolved at");
        }
        this.id = id;
        this.bookingId = bookingId;
        this.creationUserId = creationUserId;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }
}
