package com.myblogbackend.blog.config.kafka;

import org.springframework.stereotype.Component;

@Component
public class KafkaTopicManager {
    private static final String NOTIFICATION_REGISTER_TOPIC = "notification-register-topic";

    public String getNotificationRegisterTopic() {
        return NOTIFICATION_REGISTER_TOPIC;
    }

    // You can add more topic management methods here
}
