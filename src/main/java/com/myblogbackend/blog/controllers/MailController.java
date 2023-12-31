package com.myblogbackend.blog.controllers;


import com.myblogbackend.blog.services.impl.MailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MailController {
    private final Logger log = LogManager.getLogger(MailController.class);
    private final MailServiceImpl mailServiceImpl;

}
