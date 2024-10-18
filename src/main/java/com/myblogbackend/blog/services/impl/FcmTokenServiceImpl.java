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
        var userDeviceFireBaseTokenEntity = userFirebaseDeviceTokenMapper
                .toUserFirebaseDeviceTokenEntity(userFirebaseDeviceRequest);

        var existingToken = firebaseUserRepository.findByUserIdAndDeviceToken(
                userFirebaseDeviceRequest.getUserId(),
                userFirebaseDeviceRequest.getDeviceToken()
        );

        if (existingToken.isPresent()) {
            var existingEntity = existingToken.get();
            firebaseUserRepository.save(existingEntity);
            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(existingEntity);
        } else {
            var result = firebaseUserRepository.save(userDeviceFireBaseTokenEntity);
            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(result);
        }
    }
}
