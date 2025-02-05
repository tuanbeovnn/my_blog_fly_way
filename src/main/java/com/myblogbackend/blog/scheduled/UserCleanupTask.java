package com.myblogbackend.blog.scheduled;

import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.UserTokenRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCleanupTask {
    private static final Logger logger = LoggerFactory.getLogger(UserCleanupTask.class);
    private final UsersRepository userRepository;
    private final UserTokenRepository verificationTokenRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanupInactiveUsers() {
        logger.info("Starting user cleanup task...");

        List<UserEntity> activeUsers = userRepository.findAllByActiveIsTrue();

        for (UserEntity user : activeUsers) {
            Date thresholdTime = new Date(user.getCreatedDate().getTime() + (15 * 60 * 1000));
            Date currentTime = new Date();

            if (currentTime.after(thresholdTime)) {
                logger.info("Cleaning up user: {}", user.getEmail());
                // Delete associated verification tokens first
                verificationTokenRepository.deleteByUserId(user.getId());
                // Mark user as inactive and remove from database
                user.setActive(false);
                userRepository.delete(user);
                // Alternatively, perform other actions like sending a reminder email
                logger.info("User cleanup completed for: {}", user.getEmail());
            }
        }
        logger.info("User cleanup task completed.");
    }

}
