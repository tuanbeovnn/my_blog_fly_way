package com.myblogbackend.blog.strategyPatternV2;


import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "email")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class EmailProperties {

    private Email registrationConfirmation;

    @Getter
    @AllArgsConstructor
    public static class Email {
        private String template;
        private String subject;
        private String from;
        private String baseUrl;
    }

}
