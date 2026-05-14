package com.efeerturk.advanced_complete_redis.controller;

import com.efeerturk.advanced_complete_redis.service.AnalyticsService;
import com.efeerturk.advanced_complete_redis.service.OrderService;
import com.efeerturk.advanced_complete_redis.service.ProductService;
import com.efeerturk.advanced_complete_redis.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/flash-sale")
@RequiredArgsConstructor
public class RestFlashSaleController {

    private final OrderService orderService;
    private final RateLimiterService rateLimiterService;
    private final AnalyticsService analyticsService;
    private final ProductService productService;


    @GetMapping("/product/{id}")
    public ResponseEntity<?> viewProduct(@PathVariable Long id, @RequestParam String userIp) {

        analyticsService.recordProductView(id, userIp);


        return ResponseEntity.ok(productService.getProduct(id));
    }


    @PostMapping("/purchase")
    public ResponseEntity<String> buyProduct(
            @RequestParam Long productId,
            @RequestParam String userId,
            @RequestParam int quantity) {


        if (!rateLimiterService.canPurchase(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Çok hızlı işlem yapıyorsunuz. Lütfen 30 saniye bekleyin!");
        }


        String result = orderService.processPurchase(productId, quantity);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/product/{id}/stats")
    public ResponseEntity<String> getProductStats(@PathVariable Long id) {
        Long uniqueViewers = analyticsService.getUniqueViewers(id);
        return ResponseEntity.ok("Bu ürünü bugün " + uniqueViewers + " farklı kişi inceledi.");
    }
}
