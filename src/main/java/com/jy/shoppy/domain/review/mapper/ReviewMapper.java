package com.jy.shoppy.domain.review.mapper;

import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.review.dto.CreateReviewResponse;
import com.jy.shoppy.domain.review.dto.ReviewResponse;
import com.jy.shoppy.domain.review.dto.ReviewableProductResponse;
import com.jy.shoppy.domain.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    // ===== 작성된 리뷰 매핑 =====
    @Mapping(target = "reviewId", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "sizeRating", source = "sizeRating")
    @Mapping(target = "colorRating", source = "colorRating")
    @Mapping(target = "thicknessRating", source = "thicknessRating")
    @Mapping(target = "imageUrls", expression = "java(mapImageUrls(review))")
    @Mapping(target = "helpfulCount", expression = "java(review.getHelpfulCount())")
    @Mapping(target = "commentCount", expression = "java(review.getCommentCount())")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);

    // ===== 리뷰 작성 응답 매핑 =====
    @Mapping(target = "reviewId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "createdAt", source = "createdAt")
    CreateReviewResponse toCreateResponse(Review review);

    // ===== Helper 메서드 =====
    default List<String> mapImageUrls(Review review) {
        if (review.getImages() == null || review.getImages().isEmpty()) {
            return List.of();
        }
        return review.getImages().stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());
    }

    /**
     * OrderProduct → ReviewableProductResponse 매핑
     */
    @Mapping(source = "id", target = "orderProductId")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderDate", target = "orderDate")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "productPrice")
    @Mapping(target = "thumbnailUrl", expression = "java(orderProduct.getProduct().getThumbnailUrl())")
    @Mapping(source = "selectedColor", target = "color")
    @Mapping(source = "selectedSize", target = "size")
    @Mapping(source = "quantity", target = "quantity")
    ReviewableProductResponse toReviewableResponse(OrderProduct orderProduct);

    List<ReviewableProductResponse> toReviewableResponseList(List<OrderProduct> orderProducts);
}
