package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.CarEvent;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Mock
    private Location locationMock;

    @InjectMocks
    private CarEventPublisher carEventPublisher;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<CarEvent> eventCaptor;

    private final String testTopic = "test.car.events";
    private Car testCar;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carEventPublisher, "carEventsTopic", testTopic);
        
        when(locationMock.getId()).thenReturn(100L);

        testCar = new Car();
        testCar.setId(1L);
        testCar.setOwnerId(10L);
        testCar.setLocation(locationMock);
        testCar.setMake("TestMake");
        testCar.setModel("TestModel");
        testCar.setStatus(CarStatus.AVAILABLE);
        testCar.setVerificationStatus(VerificationStatus.VERIFIED);
        testCar.setType(CarType.SEDAN);
        testCar.setPrice(new BigDecimal("50.00"));
    }

    private CompletableFuture<SendResult<String, Object>> mockKafkaSend(boolean success) {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        if (success) {
            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(testTopic, "key", new CarEvent());
            TopicPartition topicPartition = new TopicPartition(testTopic, 0);
            RecordMetadata recordMetadata = new RecordMetadata(topicPartition, 0, 0, System.currentTimeMillis(), 0, 0);
            SendResult<String, Object> sendResult = new SendResult<>(producerRecord, recordMetadata);
            future.complete(sendResult);
        } else {
            future.completeExceptionally(new RuntimeException("Kafka send failed"));
        }
        when(kafkaTemplate.send(anyString(), anyString(), any(CarEvent.class))).thenReturn(future);
        return future;
    }

    @Test
    @DisplayName("publishCarCreated sends correct event")
    void publishCarCreated_sendsCorrectEvent() {
        mockKafkaSend(true);
        carEventPublisher.publishCarCreated(testCar);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(testTopic, topicCaptor.getValue());
        assertEquals(String.valueOf(testCar.getId()), keyCaptor.getValue());
        CarEvent capturedEvent = eventCaptor.getValue();
        assertEquals("CAR_CREATED", capturedEvent.getType());
        assertEquals(testCar.getId(), capturedEvent.getCarId());
        assertEquals(testCar.getOwnerId(), capturedEvent.getOwnerId());
        assertEquals(testCar.getLocation().getId(), capturedEvent.getLocationId());
        assertEquals(testCar.getMake(), capturedEvent.getMake());
        assertEquals(testCar.getModel(), capturedEvent.getModel());
        assertEquals(testCar.getStatus().name(), capturedEvent.getStatus());
        assertEquals(testCar.getVerificationStatus().name(), capturedEvent.getVerificationStatus());
        assertEquals(testCar.getType().name(), capturedEvent.getTypeEnum());
        assertEquals(testCar.getPrice().doubleValue(), capturedEvent.getPricePerDay());
        assertNotNull(capturedEvent.getOccurredAt());
    }

    @Test
    @DisplayName("publishCarUpdated sends correct event")
    void publishCarUpdated_sendsCorrectEvent() {
        mockKafkaSend(true);
        carEventPublisher.publishCarUpdated(testCar);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(testTopic, topicCaptor.getValue());
        assertEquals(String.valueOf(testCar.getId()), keyCaptor.getValue());
        assertEquals("CAR_UPDATED", eventCaptor.getValue().getType());
        assertEquals(testCar.getId(), eventCaptor.getValue().getCarId());
    }

    @Test
    @DisplayName("publishCarDeleted sends correct event")
    void publishCarDeleted_sendsCorrectEvent() {
         mockKafkaSend(true);
        Long carIdToDelete = 99L;
        carEventPublisher.publishCarDeleted(carIdToDelete);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(testTopic, topicCaptor.getValue());
        assertEquals(String.valueOf(carIdToDelete), keyCaptor.getValue());
        assertEquals("CAR_DELETED", eventCaptor.getValue().getType());
        assertEquals(carIdToDelete, eventCaptor.getValue().getCarId());
        assertNotNull(eventCaptor.getValue().getOccurredAt());
        assertNull(eventCaptor.getValue().getOwnerId());
    }

    @Test
    @DisplayName("build handles null values in Car object including location")
    void build_handlesNulls() {
        mockKafkaSend(true);
        Car carWithNulls = new Car();
        carWithNulls.setId(2L);
        carWithNulls.setLocation(null);

        CarEvent event = (CarEvent) ReflectionTestUtils.invokeMethod(carEventPublisher, "build", carWithNulls, "CAR_CREATED");

        assertNotNull(event);
        assertEquals("CAR_CREATED", event.getType());
        assertEquals(2L, event.getCarId());
        assertNull(event.getOwnerId());
        assertNull(event.getLocationId());
        assertNull(event.getMake());
        assertNull(event.getModel());
        assertNull(event.getStatus());
        assertNull(event.getVerificationStatus());
        assertNull(event.getTypeEnum());
        assertNull(event.getPricePerDay());

        carEventPublisher.publishCarCreated(carWithNulls);
        verify(kafkaTemplate).send(eq(testTopic), eq(String.valueOf(carWithNulls.getId())), any(CarEvent.class));
    }

     @Test
     @DisplayName("publish handles Kafka send failure")
     void publish_handlesKafkaFailure() {
         CompletableFuture<SendResult<String, Object>> failedFuture = mockKafkaSend(false);
         carEventPublisher.publishCarCreated(testCar);

         assertTrue(failedFuture.isCompletedExceptionally());
         verify(kafkaTemplate).send(eq(testTopic), eq(String.valueOf(testCar.getId())), any(CarEvent.class));
     }

     @Test
     @DisplayName("publish uses default key 'car' when carId is null in event")
     void publish_nullCarId_usesDefaultKey() {
         mockKafkaSend(true);
         CarEvent eventWithNullId = CarEvent.builder()
                 .type("SOME_EVENT")
                 .occurredAt(Instant.now())
                 .build();

         ReflectionTestUtils.invokeMethod(carEventPublisher, "publish", eventWithNullId);

         verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
         assertEquals(testTopic, topicCaptor.getValue());
         assertEquals("car", keyCaptor.getValue());
         assertEquals(eventWithNullId, eventCaptor.getValue());
     }
}