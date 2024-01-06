package com.myblogbackend.blog.strategyPattern;

import com.myblogbackend.blog.config.mail.EmailProperties;
import com.myblogbackend.blog.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GmailStrategy implements MailStrategy {
    private final Logger log = LogManager.getLogger(GmailStrategy.class);
    private static final String CONFIRMATION_TOKEN = "confirmationToken";
    private static final String UNKNOWN_EMAIL = "unknown";
    private final EmailProperties emailProperties;
    private final NotificationMessageMapper notificationMessageMapper;
    private final JavaEmailSendHandler javaEmailSendHandler;

    @Override
    public void sendActivationEmail(final UserEntity user, final String token) {
        var confirmationURL = String.format(emailProperties.getRegistrationConfirmation().getBaseUrl(), token);
        Map<String, Object> dataBindings = Map.of(CONFIRMATION_TOKEN, confirmationURL);
        var simpleMailMessage = notificationMessageMapper.toSimpleMailMessage(emailProperties.getRegistrationConfirmation());
        simpleMailMessage.setTo(user.getEmail());
        try {
            javaEmailSendHandler.send(emailProperties.getRegistrationConfirmation().getTemplate(), simpleMailMessage, dataBindings);
            log.info("Sending activation email to '{}'", user.getEmail());
        } catch (Exception e) {
            logErrorSendingEmail(e, simpleMailMessage);
        }
    }

    private void logErrorSendingEmail(final Exception e, final SimpleMailMessage simpleMailMessage) {
        log.error("Error sending email to '{}'", Optional.ofNullable(simpleMailMessage.getTo())
                .map(Arrays::toString)
                .orElseGet(() -> UNKNOWN_EMAIL), e);
    }
}
