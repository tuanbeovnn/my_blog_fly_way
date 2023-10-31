package com.myblogbackend.blog.strategyPatternV2;

import com.myblogbackend.blog.models.UserEntity;
import org.springframework.scheduling.annotation.Async;

public interface MailStrategy {

//    /**
//     * Sends a notification.
//     * It can be customized and have parameters as input,
//     * can also be modified to have a return type.
//     */
//    @Async
//    void sendNotification();

    void sendActivationEmail(String email);



}