package com.myblogbackend.blog.strategyPatternV2;

import com.myblogbackend.blog.models.UserEntity;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class GmailStrategy implements MailStrategy {
    private final Logger log = LoggerFactory.getLogger(GmailStrategy.class);
    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final Environment environment;


    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.info("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart, isHtml, to, subject, content);
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(environment.getRequiredProperty("ADDRESS"));
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.info("Sent email to User '{}'", to);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }

    public void sendEmailFromTemplate(UserEntity user, String templateName, String titleKey) {
        Locale locale = Locale.getDefault();
        Context context = new Context();
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, environment.getRequiredProperty("BASE_URL"));
        String content = templateEngine.process(templateName, context);
        String subject = environment.getRequiredProperty(titleKey);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    /**
     * Using for testing
     * @param email
     * @param templateName
     * @param titleKey
     */
    public void sendEmailFromTemplateV2(String email, String templateName, String titleKey) {
        Locale locale = Locale.getDefault();
        Context context = new Context();
        context.setVariable(USER, email);
        context.setVariable(BASE_URL, environment.getRequiredProperty("BASE_URL"));
        String content = templateEngine.process(templateName, context);
        String subject = environment.getRequiredProperty(titleKey);
        sendEmail(email, subject, content, false, true);
    }

    @Override
    public void sendActivationEmail(UserEntity user) {
        log.info("GmailStrategy====> Sending activation email to '{}'", user);
        sendEmailFromTemplate(user, "mail/activationEmail", "EMAIL_ACTIVATION");
    }

}
