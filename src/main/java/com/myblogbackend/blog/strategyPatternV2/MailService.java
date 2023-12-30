package com.myblogbackend.blog.strategyPatternV2;

import com.myblogbackend.blog.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MailService {
    private final Logger log = LoggerFactory.getLogger(MailService.class);
    private final MailStrategy mailStrategy;

    public MailService(MailStrategy mailStrategy) {
        this.mailStrategy = mailStrategy;
    }

    public void sendActivationEmail(UserEntity user) {
        log.info("Sending activation email to '{}'", user.getEmail());
        mailStrategy.sendActivationEmail(user);
    }

}