package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.dto.BookingMessageCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingMessageResponseDto;

import java.util.List;

public interface BookingMessageService {

    BookingMessageResponseDto postMessage(Long bookingId, Long senderUserId, BookingMessageCreateRequestDto dto);

    List<BookingMessageResponseDto> listMessagesAfter(Long bookingId, Long readerUserId, long afterId);
}
