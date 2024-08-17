package com.myblogbackend.blog.strategyPattern;

import com.myblogbackend.blog.models.UserEntity;
import freemarker.template.TemplateException;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;

public interface MailStrategy {
    @Async
    void sendActivationEmail(UserEntity user, String token) throws TemplateException, IOException;


    @Async
    void sendForgotPasswordEmail(UserEntity user, String password) throws TemplateException, IOException;
}