package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.BookingWaitlistCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingWaitlistResponseDto;
import com.dynamiccarsharing.booking.model.BookingWaitlistEntry;
import com.dynamiccarsharing.booking.model.BookingWaitlistStatus;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.BookingWaitlistRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingWaitlistService;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingWaitlistServiceImpl implements BookingWaitlistService {

    private final BookingWaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingWaitlistResponseDto joinWaitlist(BookingWaitlistCreateRequestDto requestDto) {
        if (!bookingRepository.hasOverlappingBooking(requestDto.getCarId(), requestDto.getStartTime(), requestDto.getEndTime())) {
            throw new ValidationException("Car is currently available for this time range, waitlist is not needed.");
        }

        waitlistRepository.findActiveDuplicate(
                requestDto.getRenterId(),
                requestDto.getCarId(),
                requestDto.getStartTime(),
                requestDto.getEndTime()
        ).ifPresent(existing -> {
            throw new ValidationException("Waitlist entry already exists for this renter, car and time range.");
        });

        BookingWaitlistEntry entity = BookingWaitlistEntry.builder()
                .renterId(requestDto.getRenterId())
                .carId(requestDto.getCarId())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .pickupLocationId(requestDto.getPickupLocationId())
                .promoCode(requestDto.getPromoCode())
                .status(BookingWaitlistStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(waitlistRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingWaitlistResponseDto getActiveById(Long id) {
        return waitlistRepository.findActiveById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ValidationException("Active waitlist entry with ID " + id + " not found."));
    }

    @Override
    public BookingWaitlistResponseDto cancel(Long id) {
        BookingWaitlistEntry entry = waitlistRepository.findActiveById(id)
                .orElseThrow(() -> new ValidationException("Active waitlist entry with ID " + id + " not found."));
        entry.setStatus(BookingWaitlistStatus.CANCELED);
        return toDto(waitlistRepository.save(entry));
    }

    private BookingWaitlistResponseDto toDto(BookingWaitlistEntry entry) {
        return BookingWaitlistResponseDto.builder()
                .id(entry.getId())
                .renterId(entry.getRenterId())
                .carId(entry.getCarId())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .pickupLocationId(entry.getPickupLocationId())
                .promoCode(entry.getPromoCode())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
