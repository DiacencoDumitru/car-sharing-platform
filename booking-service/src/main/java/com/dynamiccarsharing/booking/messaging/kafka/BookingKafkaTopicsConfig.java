package com.dynamiccarsharing.booking.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class BookingKafkaTopicsConfig {

    @Bean
    public NewTopic bookingRemindersTopic(
            @Value("${application.messaging.topics.booking-reminders:booking.reminders}") String name) {
        return new NewTopic(name, 1, (short) 1);
    }
}
