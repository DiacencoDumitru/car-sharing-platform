package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode
public class Dispute {
    private final Long id;
    private final Long bookingId;
    private final Long creationUserId;
    @With
    private final String description;
    @With
    private final DisputeStatus status;
    private final LocalDateTime createdAt;
    @With
    private final LocalDateTime resolvedAt;

    public Dispute(Long id, Long bookingId, Long creationUserId, String description, DisputeStatus status, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        if (id != null) {
            Validator.validateId(id, "ID");
        }
        Validator.validateId(bookingId, "Booking ID");
        Validator.validateId(creationUserId, "Creation User ID");
        Validator.validateOptionalString(description, "Description");
        Validator.validateNonNull(status, "Status");
        Validator.validateNonNull(createdAt, "Created at");
        this.id = id;
        this.bookingId = bookingId;
        this.creationUserId = creationUserId;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public void validate() {
        if (status == DisputeStatus.RESOLVED) {
            Validator.validateNonNull(resolvedAt, "Resolved at");
        }
    }
}
