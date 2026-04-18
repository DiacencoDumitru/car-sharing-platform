package com.dynamiccarsharing.booking.messaging.outbox;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
        partitions = 1,
        topics = "booking.commands",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles({"integration", "jpa"})
@TestPropertySource(properties = {
        "application.messaging.kafka.enabled=true",
        "spring.task.scheduling.enabled=false",
        "eureka.client.enabled=false"
})
class BookingLifecycleOutboxIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingLifecycleOutboxRepository outboxRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingLifecycleOutboxRelayProcessor relayProcessor;

    @BeforeEach
    void clean() {
        outboxRepository.deleteAll();
        bookingRepository.findAll().forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }

    @Test
    @DisplayName("После approve в outbox одна запись; relay публикует в Kafka и удаляет строку")
    void approveBooking_thenRelay_clearsOutbox() throws InterruptedException {
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(10L)
                .carId(20L)
                .pickupLocationId(30L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        bookingService.approveBooking(booking.getId());

        assertThat(outboxRepository.findAll()).hasSize(1);

        long deadline = System.currentTimeMillis() + 30_000L;
        while (outboxRepository.count() > 0 && System.currentTimeMillis() < deadline) {
            relayProcessor.processOne();
            Thread.sleep(200);
        }
        assertThat(outboxRepository.findAll()).isEmpty();
    }
}
