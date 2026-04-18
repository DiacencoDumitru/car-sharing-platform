package com.dynamiccarsharing.car.messaging.kafka;

import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest(
        classes = BookingLifecycleCommandListener.class
)
@TestPropertySource(properties = {
        "application.messaging.kafka.enabled=true"
})
class BookingLifecycleCommandListenerMockIntegrationTest {

    @MockBean
    private CarService carService;

    @Autowired
    private BookingLifecycleCommandListener listener;

    @Test
    void approvedBooking_callsRentCar_whenCarAvailable() {
        Long carId = 1L;

        CarDto car = new CarDto();
        car.setId(carId);
        car.setStatus(CarStatus.AVAILABLE);
        car.setType(CarType.SEDAN);
        car.setVerificationStatus(VerificationStatus.VERIFIED);

        BookingLifecycleEventDto event = BookingLifecycleEventDto.builder()
                .bookingId(10L)
                .renterId(100L)
                .carId(carId)
                .bookingStatus(TransactionStatus.APPROVED)
                .build();

        when(carService.getByIdOrNull(carId)).thenReturn(car);

        listener.handleBookingLifecycleEvent(event);

        verify(carService, times(1)).rentCar(carId);
        verify(carService, never()).returnCar(anyLong());
    }

    @Test
    void completedBooking_callsReturnCar_whenCarRented() {
        Long carId = 2L;

        CarDto car = new CarDto();
        car.setId(carId);
        car.setStatus(CarStatus.RENTED);
        car.setType(CarType.SEDAN);
        car.setVerificationStatus(VerificationStatus.VERIFIED);

        BookingLifecycleEventDto event = BookingLifecycleEventDto.builder()
                .bookingId(20L)
                .renterId(200L)
                .carId(carId)
                .bookingStatus(TransactionStatus.COMPLETED)
                .build();

        when(carService.getByIdOrNull(carId)).thenReturn(car);

        listener.handleBookingLifecycleEvent(event);

        verify(carService, times(1)).returnCar(carId);
        verify(carService, never()).rentCar(anyLong());
    }
}

