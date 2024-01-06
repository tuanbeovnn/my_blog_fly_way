package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.services.EmailSendingService;
import com.myblogbackend.blog.strategyPattern.MailStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;


@Service
public class MailServiceImpl implements EmailSendingService {
    private final Logger log = LogManager.getLogger(MailServiceImpl.class);
    private final MailStrategy mailStrategy;

    public MailServiceImpl(final MailStrategy mailStrategy) {
        this.mailStrategy = mailStrategy;
    }

    @Override
    public void sendActivationEmail(final UserEntity user) {

    }

}