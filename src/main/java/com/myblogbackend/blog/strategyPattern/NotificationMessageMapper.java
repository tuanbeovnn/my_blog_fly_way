package com.myblogbackend.blog.strategyPattern;


import com.myblogbackend.blog.config.mail.EmailProperties;
import org.mapstruct.Mapper;
import org.springframework.mail.SimpleMailMessage;

@Mapper(componentModel = "spring")
public interface NotificationMessageMapper {
    SimpleMailMessage toSimpleMailMessage(EmailProperties.EmailInfo emailProperties);
}
