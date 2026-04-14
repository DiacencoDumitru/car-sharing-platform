package com.dynamiccarsharing.notification.messaging;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class KafkaConfig {

    @Value("${application.messaging.topics.booking-commands-dlt:booking.commands.dlt}")
    private String bookingCommandsDltTopic;

    @Bean
    public NewTopic bookingCommandsDltTopic() {
        return new NewTopic(bookingCommandsDltTopic, 1, (short) 1);
    }

    @Bean
    public ConsumerFactory<String, String> bookingLifecycleConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new java.util.HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        if (kafkaProperties.getConsumer().getGroupId() != null) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        }

        if (kafkaProperties.getConsumer().getAutoOffsetReset() != null) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        }

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> bookingLifecycleKafkaListenerContainerFactory(
            ConsumerFactory<String, String> bookingLifecycleConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(bookingLifecycleConsumerFactory);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate, bookingCommandsDltTopic));
        return factory;
    }

    private CommonErrorHandler errorHandler(KafkaTemplate<String, Object> template, String dltTopic) {
        var backoff = new ExponentialBackOffWithMaxRetries(5);
        backoff.setInitialInterval(500);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(5000);

        var recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(dltTopic, 0));

        return new DefaultErrorHandler(recoverer, backoff);
    }
}

