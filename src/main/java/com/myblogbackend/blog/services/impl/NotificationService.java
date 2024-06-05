package com.myblogbackend.blog.services.impl;


import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.myblogbackend.blog.mapper.UserFirebaseDeviceTokenMapper;
import com.myblogbackend.blog.models.UserDeviceFireBaseTokenEntity;
import com.myblogbackend.blog.repositories.FirebaseUserRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.AllDevicesNotificationRequest;
import com.myblogbackend.blog.request.DeviceNotificationRequest;
import com.myblogbackend.blog.request.NotificationSubscriptionRequest;
import com.myblogbackend.blog.request.TopicNotificationRequest;
import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LogManager.getLogger(NotificationService.class);

    private final FirebaseApp firebaseApp;
    private final UsersRepository usersRepository;
    private final UserFirebaseDeviceTokenMapper userFirebaseDeviceTokenMapper;
    private final FirebaseUserRepository firebaseUserRepository;

    public void sendNotificationToDevice(final DeviceNotificationRequest request)
            throws FirebaseMessagingException, ExecutionException, InterruptedException {
        Message fcmMessage = Message.builder()
                .setToken(request.getDeviceToken())
                .setNotification(
                        Notification.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getBody())
                                .setImage(request.getImageUrl())
                                .build()
                )
                .putAllData(request.getData())
                .build();

        String response = FirebaseMessaging.getInstance(firebaseApp).sendAsync(fcmMessage).get();
        log.info("sendNotificationToDevice response: {}", response);
    }

    // 2 users are using the same device

    // The first user login to device A and check if another user B login to device A. Update user B with that device.

    // in case, if the user A and the user B are using different browsers, send to deviceId and exactly userId

    // send notification exactly to userA and device A


    // a new system will receive userId and deviceId and send notifications

    // has a switch case for each topic send ex. new comment from a new user, new post from user who followed by yourself.


    public void sendNotificationWhenHaveANewPost(final UUID userId, final String fireBaseToken, final String topic) {
        // find user from firebase token table

        // send notification with specific topics such as create a new post
    }

    public void saveUserIdAndFireBaseToken(final UserFirebaseDeviceRequest userFirebaseDeviceRequest) {
        // save deviceId and userId in fire base device token table after user login to the system.
        UserDeviceFireBaseTokenEntity userDeviceFireBaseTokenEntity = userFirebaseDeviceTokenMapper.toUserFirebaseDeviceTokenEntity(userFirebaseDeviceRequest);
        var createUserDeviceFireBaseToken = firebaseUserRepository.save(userDeviceFireBaseTokenEntity);
        log.info("Create user fire base token successfully!");
    }

    public void sendPushNotificationToTopic(final TopicNotificationRequest request)
            throws FirebaseMessagingException, ExecutionException, InterruptedException {
        Message fcmMessage = Message.builder()
                .setTopic(request.getTopicName())
                .setNotification(
                        Notification.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getBody())
                                .setImage(request.getImageUrl())
                                .build()
                )
                .setAndroidConfig(getAndroidConfig(request.getTopicName()))
                .setApnsConfig(getApnsConfig(request.getTopicName()))
                .putAllData(request.getData())
                .build();

        String response = FirebaseMessaging.getInstance(firebaseApp).sendAsync(fcmMessage).get();
        log.info("sendNotificationToDevice response: {}", response);
    }


    public void sendMulticastNotification(final AllDevicesNotificationRequest request) throws FirebaseMessagingException {
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .addAllTokens(request.getDeviceTokenList().isEmpty() ? getAllDeviceTokens() : request.getDeviceTokenList())
                .setNotification(
                        Notification.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getBody())
                                .setImage(request.getImageUrl())
                                .build()
                )
                .putAllData(request.getData())
                .build();

        BatchResponse response = FirebaseMessaging.getInstance(firebaseApp).sendEachForMulticast(multicastMessage);
        // Process the response
        for (SendResponse sendResponse : response.getResponses()) {
            if (sendResponse.isSuccessful()) {
                log.info("Message sent successfully to: {}", sendResponse.getMessageId());
            } else {
                log.info("Failed to send message to: {}", sendResponse.getMessageId());
                log.error("Error details: {}", sendResponse.getException().getMessage());
            }
        }
    }

    public void subscribeDeviceToTopic(final NotificationSubscriptionRequest request) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().subscribeToTopic(
                Collections.singletonList(request.getDeviceToken()),
                request.getTopicName()
        );
    }

    public void unsubscribeDeviceFromTopic(final NotificationSubscriptionRequest request) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(
                Collections.singletonList(request.getDeviceToken()),
                request.getTopicName()
        );
    }

    private List<String> getAllDeviceTokens() {
        // Implement logic to retrieve all device tokens from your database or storage
        // Return a list of device tokens
        return new ArrayList<>();
    }

    private AndroidConfig getAndroidConfig(final String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound("default")
                        .setColor("#FFFF00").setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(final String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
}
