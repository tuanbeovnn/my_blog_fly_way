package com.myblogbackend.blog.controllers;


import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.strategyPatternV2.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MailController {
    private final Logger log = LoggerFactory.getLogger(MailController.class);
    private final MailService mailService;

    @PostMapping("/posts")
    @Transactional
    public void uploadInvestigation(@RequestBody MailRequest mailRequest) {
        log.info("REST request to send activation mail");
        //upload files
        mailService.sendActivationEmail(mailRequest.getEmail());
    }
}
