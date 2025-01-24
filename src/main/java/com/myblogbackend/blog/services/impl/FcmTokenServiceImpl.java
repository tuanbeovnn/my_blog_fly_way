package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.event.dto.NotificationEvent;
import com.myblogbackend.blog.feign.OutboundNotificationsList;
import com.myblogbackend.blog.mapper.UserFirebaseDeviceTokenMapper;
import com.myblogbackend.blog.repositories.FirebaseUserRepository;
import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import com.myblogbackend.blog.response.UserFirebaseDeviceResponse;
import com.myblogbackend.blog.services.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {

    private final static Logger LOGGER = LogManager.getLogger(FcmTokenServiceImpl.class);

    private final FirebaseUserRepository firebaseUserRepository;

    private final UserFirebaseDeviceTokenMapper userFirebaseDeviceTokenMapper;

    private final OutboundNotificationsList outboundNotificationsList;

    @Override
    public UserFirebaseDeviceResponse saveFcmUserTokenDevice(final UserFirebaseDeviceRequest userFirebaseDeviceRequest) {
        // Check if the user already has an FCM token stored for the specified device
        var existingToken = firebaseUserRepository.findByUserId(userFirebaseDeviceRequest.getUserId());

        // If the user already has a token stored, update it if it's different
        if (existingToken.isPresent()) {
            var existingEntity = existingToken.get();
            // Update only if the new token is different
            if (!existingEntity.getDeviceToken().equals(userFirebaseDeviceRequest.getDeviceToken())) {
                existingEntity.setDeviceToken(userFirebaseDeviceRequest.getDeviceToken());
                firebaseUserRepository.save(existingEntity);
            }

            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(existingEntity);
        } else {
            // If no existing token is found for the user, save a new token
            var userDeviceFireBaseTokenEntity = userFirebaseDeviceTokenMapper
                    .toUserFirebaseDeviceTokenEntity(userFirebaseDeviceRequest);
            var result = firebaseUserRepository.save(userDeviceFireBaseTokenEntity);

            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(result);
        }
    }

    @Override
    public List<NotificationEvent> notificationEvent(final UUID userId) {
        try {
            return outboundNotificationsList.getListNotificationByUserId(userId);
        } catch (Exception e) {
            LOGGER.error("Error occurred while get list", e);
            throw new RuntimeException("Failed to get list", e);
        }
    }
}
