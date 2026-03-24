package com.dynamiccarsharing.booking.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingLifecycleOutboxRepository extends JpaRepository<BookingLifecycleOutbox, Long> {

    @Query(
            value = "SELECT * FROM booking_lifecycle_outbox o ORDER BY o.id ASC LIMIT :limit FOR UPDATE",
            nativeQuery = true
    )
    List<BookingLifecycleOutbox> lockBatch(@Param("limit") int limit);
}
