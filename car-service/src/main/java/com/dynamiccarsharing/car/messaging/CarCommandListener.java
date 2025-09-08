package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.PriceUpdateCommand;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarCommandListener {

    private final CarService carService;

    @Value("${application.messaging.topics.car-commands:car.commands}")
    private String commandsTopic;

    @KafkaListener(
            topics = "${application.messaging.topics.car-commands:car.commands}",
            containerFactory = "priceUpdateKafkaListenerContainerFactory",
            concurrency = "2"
    )
    public void handlePriceUpdate(PriceUpdateCommand cmd) {
        log.info("Received PriceUpdateCommand for carId={}, newPrice={} (reqId={})",
                cmd.getCarId(), cmd.getNewPrice(), cmd.getRequestId());
        carService.updatePrice(cmd.getCarId(), cmd.getNewPrice());
    }
}
