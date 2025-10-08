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
            LOGGER.debug("FCM token already exists for user {}", userFirebaseDeviceRequest.getUserId());
            return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(existingToken.get());
        }

        var entityToSave = userFirebaseDeviceTokenMapper
                .toUserFirebaseDeviceTokenEntity(userFirebaseDeviceRequest);

        var result = firebaseUserRepository.save(entityToSave);
        LOGGER.info("New FCM token saved for user {} - device will receive notifications",
                userFirebaseDeviceRequest.getUserId());

        cleanupOldTokensIfNeeded(userFirebaseDeviceRequest.getUserId());

        return userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenResponse(result);
    }

    private void cleanupOldTokensIfNeeded(final UUID userId) {
        try {
            var allTokens = firebaseUserRepository.findAllByUserId(userId);

            if (allTokens.size() > 10) {
                var tokensToDelete = allTokens.stream()
                        .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                        .skip(10)
                        .toList();

                firebaseUserRepository.deleteAll(tokensToDelete);
                LOGGER.info("Cleaned up {} old FCM tokens for user {}", tokensToDelete.size(), userId);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to cleanup old tokens for user {}: {}", userId, e.getMessage());
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
