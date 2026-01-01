package com.jy.shoppy.domain.review.service;

import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.review.dto.ReviewResponse;
import com.jy.shoppy.domain.review.entity.Review;
import com.jy.shoppy.domain.review.mapper.ReviewMapper;
import com.jy.shoppy.domain.review.repository.ReviewRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    /**
     * 전체 리뷰 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(reviewMapper::toResponse);
    }

    /**
     * 리뷰 상세 조회
     */
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_REVIEW));

        return reviewMapper.toResponse(review);
    }

    /**
     * 리뷰 삭제 (관리자)
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_REVIEW));

        // 2. 상품 ID 미리 저장
        Long productId = review.getProduct().getId();
        Long userId = review.getUser().getId();

        // 3. 리뷰 삭제 (cascade로 이미지도 자동 삭제)
        reviewRepository.delete(review);

        // 4. 상품 평점 업데이트
        updateProductRating(productId);

        log.info("[Admin] Review deleted: reviewId={}, userId={}, productId={}",
                reviewId, userId, productId);
    }

    /**
     * 상품별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }

    /**
     * 사용자별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }

    /**
     * 상품 평균 평점 업데이트
     */
    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        // 평균 평점 계산
        BigDecimal averageRating = reviewRepository.findAverageRatingByProductId(productId)
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        // 리뷰 개수 조회
        Long reviewCount = reviewRepository.countByProductId(productId);

        // Product 엔티티 업데이트
        product.updateAverageRating(averageRating, reviewCount.intValue());
        productRepository.save(product);

        log.info("[Admin] Product rating updated: productId={}, averageRating={}, reviewCount={}",
                productId, averageRating, reviewCount);
    }
}