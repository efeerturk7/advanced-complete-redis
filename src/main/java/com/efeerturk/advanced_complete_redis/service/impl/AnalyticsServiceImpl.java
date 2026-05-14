package com.efeerturk.advanced_complete_redis.service.impl;

import com.efeerturk.advanced_complete_redis.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final StringRedisTemplate redisTemplate;
    @Override
    public void recordProductView(Long productId, String userIp) {
        String key = "analytics:product_views:" + productId;
        redisTemplate.opsForHyperLogLog().add(key, userIp);
    }
    @Override
    public Long getUniqueViewers(Long productId) {
        String key = "analytics:product_views:" + productId;
        return redisTemplate.opsForHyperLogLog().size(key);
    }
}
