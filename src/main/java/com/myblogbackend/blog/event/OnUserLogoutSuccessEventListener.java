package com.myblogbackend.blog.event;

import com.myblogbackend.blog.cache.LoggedOutJwtTokenCache;
import com.myblogbackend.blog.request.DeviceInfoRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
public class OnUserLogoutSuccessEventListener implements ApplicationListener<OnUserLogoutSuccessEvent> {

    private final LoggedOutJwtTokenCache tokenCache;
    private static final Logger logger = LogManager.getLogger(OnUserLogoutSuccessEventListener.class);

    @Autowired
    public OnUserLogoutSuccessEventListener(final LoggedOutJwtTokenCache tokenCache) {
        this.tokenCache = tokenCache;
    }

    public void onApplicationEvent(final OnUserLogoutSuccessEvent event) {
        if (null != event) {
            DeviceInfoRequest deviceInfoRequest = event.getLogOutRequest().getDeviceInfo();
            logger.info(String.format("Log out success event received for user [%s] for device [%s]", event.getUserEmail(), deviceInfoRequest));
            tokenCache.markLogoutEventForToken(event);
        }
    }
}