package com.efeerturk.advanced_complete_redis.service;

public interface OrderService {
    String processPurchase(Long productId, int quantity);
}
