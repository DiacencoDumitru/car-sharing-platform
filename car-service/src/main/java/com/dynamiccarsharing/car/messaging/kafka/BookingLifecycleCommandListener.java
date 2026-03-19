package com.dynamiccarsharing.car.messaging.kafka;

import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class BookingLifecycleCommandListener {

    private final CarService carService;

    @KafkaListener(
            topics = "${application.messaging.topics.booking-commands:booking.commands}",
            containerFactory = "bookingLifecycleKafkaListenerContainerFactory",
            concurrency = "2"
    )
    public void handleBookingLifecycleEvent(BookingLifecycleEventDto event) {
        if (event == null) {
            log.warn("Received null BookingLifecycleEventDto, ignoring.");
            return;
        }

        Long carId = event.getCarId();
        TransactionStatus bookingStatus = event.getBookingStatus();

        if (carId == null || bookingStatus == null) {
            log.warn("BookingLifecycleEventDto has missing carId/status (bookingId={}), ignoring.",
                    event.getBookingId());
            return;
        }

        CarDto car = carService.getByIdOrNull(carId);
        if (car == null || car.getStatus() == null) {
            log.warn("Car not found or has null status for carId={}, ignoring.", carId);
            return;
        }

        switch (bookingStatus) {
            case APPROVED -> {
                if (car.getStatus() == CarStatus.AVAILABLE) {
                    log.info("Renting carId={} due to booking APPROVED.", carId);
                    carService.rentCar(carId);
                }
            }
            case COMPLETED, CANCELED -> {
                if (car.getStatus() == CarStatus.RENTED) {
                    log.info("Returning carId={} due to booking {}.", carId, bookingStatus);
                    carService.returnCar(carId);
                }
            }
            default -> {
            }
        }
    }
}

