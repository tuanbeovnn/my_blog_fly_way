package com.myblogbackend.blog.services;


import com.myblogbackend.blog.models.UserEntity;

public interface EmailSendingService {

    void sendActivationEmail(UserEntity user);
}
