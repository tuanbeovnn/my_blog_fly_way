package com.myblogbackend.blog.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TopicNotificationRequest extends NotificationRequest {
    @NotBlank
    private String topicName;
}
