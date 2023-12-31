package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.services.EmailSendingService;
import com.myblogbackend.blog.strategyPattern.MailStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MailServiceImpl implements EmailSendingService {
    private final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final MailStrategy mailStrategy;

    public MailServiceImpl(final MailStrategy mailStrategy) {
        this.mailStrategy = mailStrategy;
    }

    public void sendActivationEmail(final UserEntity user) {
        log.info("Sending activation email to '{}'", user.getEmail());
        mailStrategy.sendActivationEmail(user);
    }

}