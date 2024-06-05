package com.myblogbackend.blog.request;

import com.myblogbackend.blog.enums.TopicType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TopicNotificationRequest extends NotificationRequest {
    private String deviceToken;
    private TopicType topicName;
}
