package com.jy.shoppy.domain.coupon.entity;

import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupon_templates")
public class CouponTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private Integer discountValue;

    @Column(name = "min_order_amount")
    private Integer minOrderAmount;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    @Column(name = "valid_days", nullable = false)
    private Integer validDays;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;  // NULL이면 무제한

    @Column(name = "issue_count", nullable = false)
    private Integer issueCount = 0;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @OneToMany(mappedBy = "template")
    private List<UserCoupon> userCoupons;

    @OneToMany(mappedBy = "couponTemplate")
    private List<GradeCouponPolicy> policies;

    public int calculateDiscount(int orderAmount) {
        // 최소 주문 금액 체크
        if (minOrderAmount != null && orderAmount < minOrderAmount) {
            return 0;
        }
        return discountType.calculateDiscount(
                orderAmount,
                discountValue,
                maxDiscountAmount
        );
    }

    public boolean isApplicable(int orderAmount) {
        return minOrderAmount == null || orderAmount >= minOrderAmount;
    }

    public LocalDateTime calculateExpiresAt() {
        return LocalDateTime.now().plusDays(validDays);
    }

    public boolean canIssue() {
        // 기간 체크
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        // 수량 체크
        if (usageLimit != null && issueCount >= usageLimit) {
            return false;
        }

        return true;
    }

    public void incrementIssueCount() {
        if (!canIssue()) {
            throw new IllegalStateException("쿠폰 발행이 불가능합니다.");
        }
        this.issueCount++;
    }

    public void incrementUsedCount() {
        this.usedCount++;
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();

        boolean afterStart = startDate == null || now.isAfter(startDate);
        boolean beforeEnd = endDate == null || now.isBefore(endDate);

        return afterStart && beforeEnd;
    }

    public Integer getRemainingCount() {
        if (usageLimit == null) {
            return null; // 무제한
        }
        return usageLimit - issueCount;
    }
}
