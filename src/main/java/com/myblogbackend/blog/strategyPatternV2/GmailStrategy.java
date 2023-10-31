package com.myblogbackend.blog.strategyPatternV2;


import com.myblogbackend.blog.models.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class GmailStrategy implements MailStrategy {


    @Override
    public void sendActivationEmail(UserEntity user) {

    }

    @Override
    public void sendCreationEmail(UserEntity user) {

    }

    @Override
    public void sendPasswordResetMail(UserEntity user) {

    }
}
