package com.myblogbackend.blog.controllers;


import com.myblogbackend.blog.services.impl.MailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MailController {
    private final Logger log = LoggerFactory.getLogger(MailController.class);
    private final MailServiceImpl mailServiceImpl;

}
