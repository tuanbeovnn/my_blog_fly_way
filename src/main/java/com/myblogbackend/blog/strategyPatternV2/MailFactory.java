package com.myblogbackend.blog.strategyPatternV2;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MailFactory {
    private final Logger log = LoggerFactory.getLogger(MailFactory.class);

    private final Environment environment;
    private final GmailStrategy gmailStrategy;

    public MailStrategy createStrategy() {
        String[] activeProfiles = environment.getActiveProfiles();
        log.info("Active profiles '{}'", Arrays.toString(activeProfiles));

        //Check if Active profiles contains "local" or "test"
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(
                env -> (env.equalsIgnoreCase(Constant.DEV_PROFILE)))) {
            return this.gmailStrategy;
        } else {
            return this.gmailStrategy;
        }
    }
}