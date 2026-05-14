package com.efeerturk.advanced_complete_redis.repo;

import com.efeerturk.advanced_complete_redis.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
