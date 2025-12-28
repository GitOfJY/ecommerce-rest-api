package com.jy.shoppy.domain.review.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderProductRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.review.dto.CreateReviewRequest;
import com.jy.shoppy.domain.review.dto.CreateReviewResponse;
import com.jy.shoppy.domain.review.dto.ReviewResponse;
import com.jy.shoppy.domain.review.dto.ReviewableProductResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    // TODO 리뷰 수정
    // TODO 리뷰 삭제

    /**
     * 작성 가능한 리뷰 목록 조회
     */
    // TODO 수정
//    @Transactional(readOnly = true)
//    public List<ReviewableProductResponse> getReviewableList(Account account) {
//        User user = userRepository.findById(account.getAccountId())
//                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
//
//        List<OrderProduct> orderProducts = orderRepository.findReviewableOrderProducts(user.getId());
//        return reviewMapper.toReviewableResponseList(orderProducts);
//    }

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
    public List<ReviewResponse> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviewMapper.toResponseList(reviews);
    }
}
