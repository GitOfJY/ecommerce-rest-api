package com.jy.shoppy.domain.review.entity;

import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.review.dto.CreateReviewRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "review_images")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder; // 이미지 순서

    public static ReviewImage create(Review review, String url, int orderNumber) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(url)
                .displayOrder(orderNumber)
                .build();
    }
}
