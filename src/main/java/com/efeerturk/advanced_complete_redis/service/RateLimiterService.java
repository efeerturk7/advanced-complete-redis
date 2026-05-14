package com.efeerturk.advanced_complete_redis.service;

public interface RateLimiterService {
     boolean canPurchase(String userId);
}
