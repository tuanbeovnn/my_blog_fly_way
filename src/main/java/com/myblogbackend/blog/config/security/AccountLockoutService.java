package com.myblogbackend.blog.config.security;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    public void recordFailedAttempt(final String email) {
        String key = "failed_attempts:" + email;
        String attempts = redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            redisTemplate.opsForValue().set(key, "1", LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
        } else {
            int count = Integer.parseInt(attempts);
            redisTemplate.opsForValue().set(key, String.valueOf(count + 1), LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);

            if (count + 1 >= MAX_FAILED_ATTEMPTS) {
                lockAccount(email);
            }
        }
    }

    public void clearFailedAttempts(final String email) {
        redisTemplate.delete("failed_attempts:" + email);
        redisTemplate.delete("account_locked:" + email);
    }

    public boolean isAccountLocked(final String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("account_locked:" + email));
    }

    private void lockAccount(final String email) {
        String lockKey = "account_locked:" + email;
        redisTemplate.opsForValue().set(lockKey, "true", LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
    }

    public int getFailedAttempts(final String email) {
        String attempts = redisTemplate.opsForValue().get("failed_attempts:" + email);
        return attempts != null ? Integer.parseInt(attempts) : 0;
    }
}
