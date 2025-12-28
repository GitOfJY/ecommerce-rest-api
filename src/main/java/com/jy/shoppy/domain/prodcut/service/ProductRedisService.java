package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.prodcut.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRedisService {
    private final StringRedisTemplate redisTemplate;

    private static final String PRICE_KEY = "products:price";
    private static final String RATING_KEY = "products:rating";
    private static final String DATE_KEY = "products:date";

    /**
     * 상품을 Redis ZSET에 저장
     */
    public void saveProduct(Product product) {
        String productId = String.valueOf(product.getId());

        // 1. 가격 정렬 ZSET
        redisTemplate.opsForZSet().add(PRICE_KEY, productId, product.getPrice().doubleValue());

        // 2. 평점 정렬 ZSET
        double rating = product.getAverageRating() != null
                ? product.getAverageRating().doubleValue()
                : 0.0;
        redisTemplate.opsForZSet().add(RATING_KEY, productId, rating);

        // 3. 등록일 정렬 ZSET (Unix Timestamp)
        long timestamp = product.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(DATE_KEY, productId, timestamp);

        log.info("Product saved to Redis: productId={}, price={}, rating={}, date={}",
                productId, product.getPrice(), rating, timestamp);
    }

    /**
     * 정렬된 상품 ID 목록 조회
     */
    public List<Long> getProductIds(String sortBy, Boolean ascending, Long offset, Long limit) {
        String key = getKeyBySortType(sortBy);
        Set<String> productIds;  // ✅ String으로 변경

        if (ascending != null && ascending) {
            productIds = redisTemplate.opsForZSet().range(key, offset, offset + limit - 1);
        } else {
            productIds = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);
        }

        return productIds.stream()
                .map(Long::parseLong)
                .toList();
    }

    /**
     * 가격 범위로 필터링된 상품 ID 조회
     */
    public List<Long> getProductIdsByPriceRange(Double minPrice, Double maxPrice,
                                                Boolean ascending, Long offset, Long limit) {
        Set<String> productIds;

        if (ascending != null && ascending) {
            productIds = redisTemplate.opsForZSet()
                    .rangeByScore(PRICE_KEY, minPrice, maxPrice, offset, limit);
        } else {
            productIds = redisTemplate.opsForZSet()
                    .reverseRangeByScore(PRICE_KEY, minPrice, maxPrice, offset, limit);
        }

        return productIds.stream()
                .map(Long::parseLong)
                .toList();
    }

    /**
     * 상품 삭제
     */
    public void deleteProduct(Long productId) {
        String id = String.valueOf(productId);
        redisTemplate.opsForZSet().remove(PRICE_KEY, id);
        redisTemplate.opsForZSet().remove(RATING_KEY, id);
        redisTemplate.opsForZSet().remove(DATE_KEY, id);

        log.info("Product removed from Redis: productId={}", productId);
    }

    /**
     * 정렬 타입에 따른 Redis Key 반환
     */
    private String getKeyBySortType(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "price" -> PRICE_KEY;
            case "rating" -> RATING_KEY;
            case "date" -> DATE_KEY;
            default -> throw new IllegalArgumentException("Invalid sort type: " + sortBy);
        };
    }

    /**
     * 전체 상품 수 조회
     */
    public Long getTotalCount(String sortBy) {
        String key = getKeyBySortType(sortBy);
        return redisTemplate.opsForZSet().size(key);
    }
}
