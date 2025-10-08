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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public UserFirebaseDeviceResponse saveFcmUserTokenDevice(final UserFirebaseDeviceRequest userFirebaseDeviceRequest) {
        var existingToken = firebaseUserRepository.findByUserIdAndDeviceToken(
                userFirebaseDeviceRequest.getUserId(),
                userFirebaseDeviceRequest.getDeviceToken()
        );

        if (existingToken.isPresent()) {
            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(existingToken.get());
        }

        var entityToSave = userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenEntity(userFirebaseDeviceRequest);
        var result = firebaseUserRepository.save(entityToSave);
        return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(result);
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
