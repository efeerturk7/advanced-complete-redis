package com.efeerturk.advanced_complete_redis.service;

import com.efeerturk.advanced_complete_redis.model.Product;

public interface ProductService {
     Product getProduct(Long id);
    void updateStock(Product product, int quantity);
}
