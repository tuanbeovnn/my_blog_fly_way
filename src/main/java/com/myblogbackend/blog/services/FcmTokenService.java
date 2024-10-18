package com.myblogbackend.blog.services;

import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import com.myblogbackend.blog.response.UserFirebaseDeviceResponse;

public interface FcmTokenService {
    UserFirebaseDeviceResponse saveFcmUserTokenDevice(UserFirebaseDeviceRequest userFirebaseDeviceRequest);
}
