package com.jy.shoppy.domain.review.entity;

import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.review.dto.CreateReviewRequest;
import com.jy.shoppy.domain.review.dto.UpdateReviewRequest;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_product_id", nullable = false, unique = true)
    private OrderProduct orderProduct;

    @Column(nullable = false)
    private Integer rating; // 1-5 (필수)

    @Column(name = "size_rating")
    private Integer sizeRating; // 1:매우작음, 2:작음, 3:보통, 4:큼, 5:매우큼

    @Column(name = "color_rating")
    private Integer colorRating; // 1:매우어두움, 2:어두움, 3:보통, 4:밝음, 5:매우밝음

    @Column(name = "thickness_rating")
    private Integer thicknessRating; // 1:매우얇음, 2:얇음, 3:보통, 4:두꺼움, 5:매우두꺼움

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewHelpful> helpfuls = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    public void addImage(ReviewImage image) {
        this.images.add(image);
    }

    public void clearImages() {
        this.images.clear();
    }

    public int getHelpfulCount() {
        return this.helpfuls != null ? this.helpfuls.size() : 0;
    }

    public int getCommentCount() {
        return this.comments != null ? this.comments.size() : 0;
    }

    public static Review create(CreateReviewRequest req, OrderProduct orderProduct, User user) {
        return Review.builder()
                .user(user)
                .orderProduct(orderProduct)
                .product(orderProduct.getProduct())
                .rating(req.getRating())
                .sizeRating(req.getSizeRating())
                .colorRating(req.getColorRating())
                .thicknessRating(req.getThicknessRating())
                .content(req.getContent())
                .build();
    }

    public void update(UpdateReviewRequest req) {
        this.rating = req.getRating();
        this.sizeRating = req.getSizeRating();
        this.colorRating = req.getColorRating();
        this.thicknessRating = req.getThicknessRating();
        this.content = req.getContent();
    }
}
