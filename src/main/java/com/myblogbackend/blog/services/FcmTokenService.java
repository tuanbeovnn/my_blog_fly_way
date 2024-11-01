package com.myblogbackend.blog.services;

import com.myblogbackend.blog.event.dto.NotificationEvent;
import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import com.myblogbackend.blog.response.UserFirebaseDeviceResponse;

import java.util.List;
import java.util.UUID;

public interface FcmTokenService {
    UserFirebaseDeviceResponse saveFcmUserTokenDevice(UserFirebaseDeviceRequest userFirebaseDeviceRequest);


    List<NotificationEvent> notificationEvent(UUID userId);
}
