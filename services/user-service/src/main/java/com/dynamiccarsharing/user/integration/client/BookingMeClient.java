package com.dynamiccarsharing.user.integration.client;

import com.dynamiccarsharing.user.dto.me.BookingPageResponse;
import com.dynamiccarsharing.user.dto.me.TransactionPageResponse;
import org.springframework.data.domain.Pageable;

public interface BookingMeClient {

    BookingPageResponse getUserBookings(Long userId, String asRole, Pageable pageable);

    TransactionPageResponse getUserTransactions(Long userId, Pageable pageable);
}
