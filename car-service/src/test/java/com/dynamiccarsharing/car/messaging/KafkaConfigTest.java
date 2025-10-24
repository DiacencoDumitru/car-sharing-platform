package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.PriceUpdateCommand;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.backoff.BackOff;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    @InjectMocks
    private KafkaConfig kafkaConfig;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ConsumerFactory<String, PriceUpdateCommand> consumerFactory;

    private final String carEventsTopicName = "test.car.events";
    private final String carCommandsTopicName = "test.car.commands";
    private final String carCommandsDltTopicName = "test.car.commands.dlt";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaConfig, "carEventsTopic", carEventsTopicName);
        ReflectionTestUtils.setField(kafkaConfig, "carCommandsTopic", carCommandsTopicName);
        ReflectionTestUtils.setField(kafkaConfig, "carCommandsDltTopic", carCommandsDltTopicName);
    }

    @Test
    @DisplayName("carEventsTopic bean configuration")
    void carEventsTopic_bean() {
        NewTopic topic = kafkaConfig.carEventsTopic();
        assertEquals(carEventsTopicName, topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals((short)1, topic.replicationFactor());
    }

    @Test
    @DisplayName("carCommandsTopic bean configuration")
    void carCommandsTopic_bean() {
        NewTopic topic = kafkaConfig.carCommandsTopic();
        assertEquals(carCommandsTopicName, topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals((short)1, topic.replicationFactor());
    }

    @Test
    @DisplayName("carCommandsDltTopic bean configuration")
    void carCommandsDltTopic_bean() {
        NewTopic topic = kafkaConfig.carCommandsDltTopic();
        assertEquals(carCommandsDltTopicName, topic.name());
        assertEquals(1, topic.numPartitions());
        assertEquals((short)1, topic.replicationFactor());
    }

    @Test
    @DisplayName("priceUpdateKafkaListenerContainerFactory bean configuration")
    void priceUpdateKafkaListenerContainerFactory_bean() {
        ConcurrentKafkaListenerContainerFactory<String, PriceUpdateCommand> factory =
                kafkaConfig.priceUpdateKafkaListenerContainerFactory(consumerFactory, kafkaTemplate);

        assertEquals(consumerFactory, factory.getConsumerFactory());
        assertNotNull(factory.getContainerProperties().getAckMode());
        assertNotNull(ReflectionTestUtils.getField(factory, "commonErrorHandler"));
        assertInstanceOf(DefaultErrorHandler.class, ReflectionTestUtils.getField(factory, "commonErrorHandler"));
    }
}