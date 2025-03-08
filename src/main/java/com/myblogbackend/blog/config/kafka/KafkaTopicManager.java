package com.myblogbackend.blog.config.kafka;

import org.springframework.stereotype.Component;

@Component
public class KafkaTopicManager {
    private static final String NOTIFICATION_REGISTER_TOPIC = "notification-register-topic";
    private static final String NOTIFICATION_FORGOT_PASSWORD_TOPIC = "notification-forgot-password-topic";
    private static final String CURRENTLY_PASSWORD = "notification-currently-password-topic";


    public String getNotificationRegisterTopic() {
        return NOTIFICATION_REGISTER_TOPIC;
    }

    public String getNotificationForgotPasswordTopic() {
        return NOTIFICATION_FORGOT_PASSWORD_TOPIC;
    }

    public String getNotificationCurrentlyPasswordTopic() {
        return CURRENTLY_PASSWORD;
    }

    // You can add more topic management methods here
}
