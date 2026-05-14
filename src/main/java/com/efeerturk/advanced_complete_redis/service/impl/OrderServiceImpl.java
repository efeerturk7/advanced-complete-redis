package com.efeerturk.advanced_complete_redis.service.impl;

import com.efeerturk.advanced_complete_redis.model.Product;
import com.efeerturk.advanced_complete_redis.service.OrderService;
import com.efeerturk.advanced_complete_redis.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final RedissonClient redissonClient;
    private final ProductService productService;
    @Override
    public String processPurchase(Long productId, int quantity) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {

            if (lock.tryLock(3, 15, TimeUnit.SECONDS)) {

                Product product = productService.getProduct(productId);

                if (product.getStock() >= quantity) {
                    productService.updateStock(product, quantity);
                    return "Satın alma başarılı! Kalan stok: " + product.getStock();
                } else {
                    return "Yetersiz stok!";
                }
            } else {
                return "Sistem şu an çok yoğun, lütfen bekleyin (Kilit alınamadı).";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "İşlem sırasında sunucu hatası!";
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
