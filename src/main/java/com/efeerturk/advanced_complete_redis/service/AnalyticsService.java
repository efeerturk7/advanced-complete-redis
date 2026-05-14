package com.efeerturk.advanced_complete_redis.service;

public interface AnalyticsService {
    void recordProductView(Long productId, String userIp);
    Long getUniqueViewers(Long productId);
}
