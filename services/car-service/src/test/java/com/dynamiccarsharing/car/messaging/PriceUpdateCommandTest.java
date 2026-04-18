package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.PriceUpdateCommand;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PriceUpdateCommandTest {

    @Test
    void testLombokAnnotations() {
        Long carId = 1L;
        BigDecimal newPrice = new BigDecimal("55.50");
        String requestId = "req-123";

        PriceUpdateCommand cmd1 = PriceUpdateCommand.builder()
                .carId(carId)
                .newPrice(newPrice)
                .requestId(requestId)
                .build();

        PriceUpdateCommand cmd2 = new PriceUpdateCommand(carId, newPrice, requestId);

        PriceUpdateCommand cmd3 = new PriceUpdateCommand();
        cmd3.setCarId(carId);
        cmd3.setNewPrice(newPrice);
        cmd3.setRequestId(requestId);

        assertEquals(carId, cmd1.getCarId());
        assertEquals(newPrice, cmd1.getNewPrice());
        assertEquals(requestId, cmd1.getRequestId());

        assertEquals(cmd1, cmd2);
        assertEquals(cmd1.hashCode(), cmd2.hashCode());
        assertEquals(cmd1, cmd3);
        assertEquals(cmd1.hashCode(), cmd3.hashCode());

        PriceUpdateCommand cmd4 = PriceUpdateCommand.builder().carId(99L).build();
        assertNotEquals(cmd1, cmd4);
    }
}