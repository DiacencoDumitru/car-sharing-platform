package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.PriceUpdateCommand;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarCommandListenerTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarCommandListener carCommandListener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carCommandListener, "commandsTopic", "test.car.commands");
    }

    @Test
    @DisplayName("handlePriceUpdate calls carService.updatePrice with correct arguments")
    void handlePriceUpdate_callsService() {
        Long carId = 123L;
        BigDecimal newPrice = new BigDecimal("49.99");
        String requestId = "req-abc";
        PriceUpdateCommand command = PriceUpdateCommand.builder()
                .carId(carId)
                .newPrice(newPrice)
                .requestId(requestId)
                .build();

        when(carService.updatePrice(carId, newPrice)).thenReturn(null);

        carCommandListener.handlePriceUpdate(command);

        verify(carService, times(1)).updatePrice(carId, newPrice);
    }
}