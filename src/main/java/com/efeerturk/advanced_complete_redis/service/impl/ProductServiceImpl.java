package com.efeerturk.advanced_complete_redis.service.impl;

import com.efeerturk.advanced_complete_redis.model.Product;
import com.efeerturk.advanced_complete_redis.repo.ProductRepository;
import com.efeerturk.advanced_complete_redis.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    @Override
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
    }
    @CacheEvict(value = "products", key = "#product.id")
    @Override
    public void updateStock(Product product, int quantity) {
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}
