package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.mapper.UserFirebaseDeviceTokenMapper;
import com.myblogbackend.blog.repositories.FirebaseUserRepository;
import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import com.myblogbackend.blog.response.UserFirebaseDeviceResponse;
import com.myblogbackend.blog.services.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {

    private final FirebaseUserRepository firebaseUserRepository;

    private final UserFirebaseDeviceTokenMapper userFirebaseDeviceTokenMapper;

    @Override
    public UserFirebaseDeviceResponse saveFcmUserTokenDevice(final UserFirebaseDeviceRequest userFirebaseDeviceRequest) {
        // Check if the user already has an FCM token stored
        var existingToken = firebaseUserRepository.findByUserId(userFirebaseDeviceRequest.getUserId());

        if (existingToken.isPresent()) {
            // Update the existing token with the new device token
            var existingEntity = existingToken.get();
            existingEntity.setDeviceToken(userFirebaseDeviceRequest.getDeviceToken());

            firebaseUserRepository.save(existingEntity);

            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(existingEntity);
        } else {
            // If no existing token is found for the user, save a new token
            var userDeviceFireBaseTokenEntity = userFirebaseDeviceTokenMapper
                    .toUserFirebaseDeviceTokenEntity(userFirebaseDeviceRequest);
            var result = firebaseUserRepository.save(userDeviceFireBaseTokenEntity);

            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(result);
        }
    }
}
