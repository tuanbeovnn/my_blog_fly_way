package com.myblogbackend.blog.strategyPatternV2;

import com.myblogbackend.blog.models.UserEntity;
import org.springframework.scheduling.annotation.Async;

public interface MailStrategy {
    @Async
    void sendActivationEmail(UserEntity user);

}