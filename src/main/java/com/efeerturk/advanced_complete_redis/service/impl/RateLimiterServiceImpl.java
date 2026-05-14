package com.efeerturk.advanced_complete_redis.service.impl;

import com.efeerturk.advanced_complete_redis.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {
    private final StringRedisTemplate redisTemplate;
    @Override
    public boolean canPurchase(String userId) {
        String key = "rate_limit:purchase:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(30));
        }
        return count != null && count <= 3;
    }
}
