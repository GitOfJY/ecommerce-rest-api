package com.jy.shoppy.domain.review.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderProductRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.review.dto.*;
import com.jy.shoppy.domain.review.entity.Review;
import com.jy.shoppy.domain.review.entity.ReviewImage;
import com.jy.shoppy.domain.review.mapper.ReviewMapper;
import com.jy.shoppy.domain.review.repository.ReviewRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final ReviewMapper reviewMapper;

    // 리뷰 작성
    @Transactional
    public CreateReviewResponse create(CreateReviewRequest req, Account account) {
        // 1. 사용자 조회
        User user = userRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        // 2. 주문 상품 조회
        OrderProduct orderProduct = orderProductRepository.findById(req.getOrderProductId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));

        // 3. 권한 검증 (본인 주문인지 확인)
        if (!orderProduct.getOrder().getUser().getId().equals(user.getId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED_ACCESS);
        }

        // 4. 주문 상태 검증 (완료된 주문만 리뷰 작성 가능)
        if (orderProduct.getOrder().getStatus() != OrderStatus.COMPLETED) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_REVIEW_NOT_COMPLETED);
        }

        // 5. 중복 리뷰 검증
        if (reviewRepository.existsByOrderProductId(orderProduct.getId())) {
            throw new ServiceException(ServiceExceptionCode.REVIEW_ALREADY_EXISTS);
        }

        // 6. 리뷰 생성
        Review review = Review.create(req, orderProduct, user);

        // 7. 이미지 추가
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                ReviewImage image = ReviewImage.create(review, req.getImageUrls().get(i), i);
                review.addImage(image);
            }
        }

        reviewRepository.save(review);

        // 8. 상품 평균 평점 업데이트
        updateProductRating(orderProduct.getProduct().getId());

        log.info("Review created: reviewId={}, userId={}, productId={}",
                review.getId(), user.getId(), orderProduct.getProduct().getId());

        return reviewMapper.toCreateResponse(review);
    }

    @Transactional
    public ReviewResponse update(Long reviewId, UpdateReviewRequest req, Account account) {
        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_REVIEW));

        // 2. 권한 검증 (본인 확인)
        if (!review.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 평점이 변경되었는지 확인 (평점 업데이트 필요 여부)
        boolean ratingChanged = !review.getRating().equals(req.getRating());

        // 4. 리뷰 내용 수정
        review.update(req);

        // 5. 이미지 수정 (기존 이미지 삭제 후 새로 추가)
        if (req.getImageUrls() != null) {
            review.clearImages();
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                ReviewImage image = ReviewImage.create(review, req.getImageUrls().get(i), i);
                review.addImage(image);
            }
        }

        reviewRepository.save(review);

        // 6. 평점이 변경되었으면 상품 평점 업데이트
        if (ratingChanged) {
            updateProductRating(review.getProduct().getId());
            log.info("Product rating updated due to review update: reviewId={}, productId={}",
                    reviewId, review.getProduct().getId());
        }

        return reviewMapper.toResponse(review);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void delete(Long reviewId, Account account) {
        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_REVIEW));

        // 2. 권한 검증 (본인 확인)
        if (!review.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 상품 ID 미리 저장 (삭제 후에는 접근 불가)
        Long productId = review.getProduct().getId();

        // 4. 리뷰 삭제 (cascade로 ReviewImage도 자동 삭제됨)
        reviewRepository.delete(review);

        // 5. 삭제 후 상품 평점 업데이트
        updateProductRating(productId);
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

        // TODO Redis 업데이트
        // productRedisService.deleteProduct(productId);
        // productRedisService.saveProduct(product);

        log.info("Product rating updated: productId={}, averageRating={}, reviewCount={}",
                productId, averageRating, reviewCount);
    }

    /**
     * 작성 가능한 리뷰 상품 목록 조회
     */
    public List<ReviewableProductResponse> getReviewableProducts(Account account) {
        // 1. 리뷰 작성 가능한 주문 상품 조회
        List<OrderProduct> orderProducts = orderProductRepository
                .findReviewableOrderProducts(account.getAccountId());

        // 2. DTO 변환
        List<ReviewableProductResponse> responses = reviewMapper
                .toReviewableResponseList(orderProducts);
        return responses;
    }

    /**
     * 내가 작성한 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(Account account) {
        List<Review> reviews = reviewRepository.findByUserId(account.getAccountId());
        return reviewMapper.toResponseList(reviews);
    }

    /**
     * 특정 상품의 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Integer minRating, String sort, Pageable pageable) {
        // 1. 정렬 조건 생성
        Sort sortCondition = createSortCondition(sort);

        // 2. Pageable에 정렬 적용
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortCondition
        );

        // 3. 리뷰 조회
        Page<Review> reviews;
        if (minRating != null) {
            // 평점 필터링 있음
            reviews = reviewRepository.findByProductIdAndRatingGreaterThanEqual(
                    productId, minRating, sortedPageable);
        } else {
            // 평점 필터링 없음
            reviews = reviewRepository.findByProductId(productId, sortedPageable);
        }

        // 4. DTO 변환
        return reviews.map(reviewMapper::toResponse);
    }

    /**
     * 정렬 조건 생성
     */
    private Sort createSortCondition(String sort) {
        return switch (sort.toLowerCase()) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating_high" -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "rating_low" -> Sort.by(Sort.Direction.ASC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "helpful" -> Sort.by(Sort.Direction.DESC, "helpfulCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
