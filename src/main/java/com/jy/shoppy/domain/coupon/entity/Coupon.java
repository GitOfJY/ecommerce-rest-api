package com.jy.shoppy.domain.coupon.entity;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.coupon.dto.CreateCouponRequest;
import com.jy.shoppy.domain.coupon.dto.UpdateCouponRequest;
import com.jy.shoppy.domain.coupon.entity.type.CouponApplicationType;
import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import com.jy.shoppy.domain.prodcut.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coupons")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(name = "code_prefix", length = 10)
    String codePrefix;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    DiscountType discountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false, length = 20)
    @Builder.Default
    CouponApplicationType applicationType = CouponApplicationType.ALL;

    @Column(name = "discount_value", nullable = false)
    Integer discountValue;

    @Column(name = "min_order_amount")
    @Builder.Default
    Integer minOrderAmount = 0;

    @Column(name = "max_discount_amount")
    Integer maxDiscountAmount;

    @Column(name = "valid_days", nullable = false)
    Integer validDays;

    @Column(name = "start_date")
    LocalDateTime startDate;

    @Column(name = "end_date")
    LocalDateTime endDate;

    @Column(name = "usage_limit")
    Integer usageLimit;

    @Column(name = "issue_count", nullable = false)
    @Builder.Default
    Integer issueCount = 0;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    Integer usedCount = 0;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<CouponProduct> couponProducts = new ArrayList<>();

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<CouponCategory> couponCategories = new ArrayList<>();


    public static Coupon create(CreateCouponRequest request) {
        return Coupon.builder()
                .name(request.getName())
                .codePrefix(request.getCodePrefix())
                .applicationType(request.getApplicationType())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .validDays(request.getValidDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .issueCount(0)
                .usedCount(0)
                .build();
    }

    public void update(UpdateCouponRequest request) {
        this.name = request.getName();
        this.codePrefix = request.getCodePrefix();
        this.applicationType = request.getApplicationType();
        this.discountType = request.getDiscountType();
        this.discountValue = request.getDiscountValue();
        this.minOrderAmount = request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0;
        this.maxDiscountAmount = request.getMaxDiscountAmount();
        this.validDays = request.getValidDays();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.usageLimit = request.getUsageLimit();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 할인 금액 계산
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        BigDecimal discount;

        if (this.discountType == DiscountType.FIXED) {
            // 정액 할인
            discount = BigDecimal.valueOf(this.discountValue);
        } else {
            // 퍼센트 할인 (10% → 0.10)
            BigDecimal percentage = BigDecimal.valueOf(this.discountValue)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            // 총액 * 할인율
            discount = orderAmount.multiply(percentage);

            // 최대 할인 금액 제한
            if (this.maxDiscountAmount != null) {
                BigDecimal maxDiscount = BigDecimal.valueOf(this.maxDiscountAmount);

                // 계산된 할인액이 최대 할인액보다 크면 최대 할인액 적용
                if (discount.compareTo(maxDiscount) > 0) {
                    discount = maxDiscount;
                }
            }
        }

        // 원 단위로 내림 (소수점 제거)
        return discount.setScale(0, RoundingMode.DOWN);
    }

    /**
     * 발급 수 증가
     */
    public void increaseIssueCount(int count) {
        this.issueCount += count;
    }

    /**
     * 사용 수 증가
     */
    public void increaseUsedCount() {
        this.usedCount += 1;
    }

    /**
     * 추가 발급 가능 여부
     */
    public boolean canIssueMore(int requestCount) {
        if (usageLimit == null) {
            return true;
        }
        return (issueCount + requestCount) <= usageLimit;
    }

    /**
     * 쿠폰 유효 기간 체크
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();

        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * 남은 발급 가능 개수
     */
    public int getRemainingCount() {
        if (usageLimit == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, usageLimit - issueCount);
    }

    /**
     * 특정 상품에 쿠폰 적용 가능한지 확인
     */
    public boolean isApplicableToProduct(Long productId, Long categoryId) {
        return switch (this.applicationType) {
            case ALL -> true;
            case PRODUCT -> couponProducts.stream()
                    .anyMatch(cp -> cp.getProduct().getId().equals(productId));
            case CATEGORY -> couponCategories.stream()
                    .anyMatch(cc -> cc.getCategory().getId().equals(categoryId));
        };
    }

    /**
     * 쿠폰 적용 가능 상품 추가
     */
    public void addProduct(Product product) {
        if (this.applicationType != CouponApplicationType.PRODUCT) {
            throw new IllegalStateException("상품별 쿠폰이 아닙니다.");
        }

        CouponProduct couponProduct = CouponProduct.builder()
                .coupon(this)
                .product(product)
                .build();

        this.couponProducts.add(couponProduct);
    }

    /**
     * 쿠폰 적용 가능 카테고리 추가
     */
    public void addCategory(Category category) {
        if (this.applicationType != CouponApplicationType.CATEGORY) {
            throw new IllegalStateException("카테고리별 쿠폰이 아닙니다.");
        }

        CouponCategory couponCategory = CouponCategory.builder()
                .coupon(this)
                .category(category)
                .build();

        this.couponCategories.add(couponCategory);
    }

    /**
     * 쿠폰 사용 수 감소 (주문 취소 시)
     */
    public void decreaseUsedCount() {
        if (this.usedCount > 0) {
            this.usedCount--;
        }
    }
}