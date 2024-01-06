package com.myblogbackend.blog.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AllDevicesNotificationRequest extends NotificationRequest {
    private List<String> deviceTokenList = new ArrayList<>();
}
