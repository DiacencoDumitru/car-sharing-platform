package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.BookingMessageCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingMessageResponseDto;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.BookingMessage;
import com.dynamiccarsharing.booking.repository.BookingMessageRepository;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingMessageService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingMessageServiceImpl implements BookingMessageService {

    private static final int MESSAGE_PAGE_SIZE = 100;

    private final BookingRepository bookingRepository;
    private final BookingMessageRepository bookingMessageRepository;
    private final CarIntegrationClient carIntegrationClient;

    @Override
    public BookingMessageResponseDto postMessage(Long bookingId, Long senderUserId, BookingMessageCreateRequestDto dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found."));
        assertMessagingAllowed(booking);
        assertParticipant(booking, senderUserId);

        String body = dto.getBody().trim();
        BookingMessage saved = bookingMessageRepository.save(BookingMessage.builder()
                .booking(booking)
                .senderUserId(senderUserId)
                .body(body)
                .build());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingMessageResponseDto> listMessagesAfter(Long bookingId, Long readerUserId, long afterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found."));
        assertMessagingAllowed(booking);
        assertParticipant(booking, readerUserId);

        return bookingMessageRepository
                .findByBooking_IdAndIdGreaterThanOrderByIdAsc(bookingId, afterId, PageRequest.of(0, MESSAGE_PAGE_SIZE))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private void assertMessagingAllowed(Booking booking) {
        TransactionStatus s = booking.getStatus();
        if (s == TransactionStatus.CANCELED || s == TransactionStatus.REFUNDED) {
            throw new ValidationException("Messaging is not allowed for this booking status.");
        }
    }

    private void assertParticipant(Booking booking, Long userId) {
        if (booking.getRenterId().equals(userId)) {
            return;
        }
        CarDto car = carIntegrationClient.getCarById(booking.getCarId());
        if (car.getOwnerId() != null && car.getOwnerId().equals(userId)) {
            return;
        }
        throw new AccessDeniedException("User is not a participant of this booking.");
    }

    private BookingMessageResponseDto toDto(BookingMessage m) {
        return BookingMessageResponseDto.builder()
                .id(m.getId())
                .senderUserId(m.getSenderUserId())
                .body(m.getBody())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
