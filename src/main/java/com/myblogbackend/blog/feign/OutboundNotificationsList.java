package com.myblogbackend.blog.feign;

import com.myblogbackend.blog.event.dto.NotificationEvent;
import com.myblogbackend.blog.feign.configs.UploadFilesFeignWithBasicAuthenticationConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(value = "notifications", url = "${notifications.event.url}", configuration = UploadFilesFeignWithBasicAuthenticationConfig.class)
public interface OutboundNotificationsList {
    @GetMapping(value = "/api/v1/notifications/{userId}")
    List<NotificationEvent> getListNotificationByUserId(@PathVariable(value = "userId") final UUID userId);
}