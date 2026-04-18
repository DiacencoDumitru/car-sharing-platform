package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.car.messaging.dto.PriceUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${application.messaging.topics.car-events:car.events}")
    private String carEventsTopic;

    @Value("${application.messaging.topics.car-commands:car.commands}")
    private String carCommandsTopic;

    @Value("${application.messaging.topics.car-commands-dlt:car.commands.dlt}")
    private String carCommandsDltTopic;

    @Value("${application.messaging.topics.booking-commands:booking.commands}")
    private String bookingCommandsTopic;

    @Value("${application.messaging.topics.booking-commands-dlt:booking.commands.dlt}")
    private String bookingCommandsDltTopic;

    @Bean
    public NewTopic carEventsTopic() {
        return TopicBuilder.name(carEventsTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic carCommandsTopic() {
        return TopicBuilder.name(carCommandsTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic carCommandsDltTopic() {
        return TopicBuilder.name(carCommandsDltTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic bookingCommandsTopic() {
        return TopicBuilder.name(bookingCommandsTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic bookingCommandsDltTopic() {
        return TopicBuilder.name(bookingCommandsDltTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PriceUpdateCommand> priceUpdateKafkaListenerContainerFactory(
            ConsumerFactory<String, PriceUpdateCommand> cf,
            KafkaTemplate<String, Object> template) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, PriceUpdateCommand>();
        factory.setConsumerFactory(cf);
        factory.setCommonErrorHandler(errorHandler(template));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingLifecycleEventDto> bookingLifecycleKafkaListenerContainerFactory(
            ConsumerFactory<String, BookingLifecycleEventDto> cf,
            KafkaTemplate<String, Object> template) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, BookingLifecycleEventDto>();
        factory.setConsumerFactory(cf);
        factory.setCommonErrorHandler(errorHandler(template, bookingCommandsDltTopic));
        return factory;
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        return errorHandler(template, carCommandsDltTopic);
    }

    private CommonErrorHandler errorHandler(KafkaTemplate<String, Object> template, String dltTopic) {
        var backoff = new ExponentialBackOffWithMaxRetries(5);
        backoff.setInitialInterval(500);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(5000);

        var recoverer = new DeadLetterPublishingRecoverer(template, (record, ex) -> {
            return new TopicPartition(dltTopic, 0);
        });

        return new DefaultErrorHandler(recoverer, backoff);
    }
}
