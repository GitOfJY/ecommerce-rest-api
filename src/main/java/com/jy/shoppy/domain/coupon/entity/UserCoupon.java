package com.jy.shoppy.domain.coupon.entity;

import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status = CouponStatus.AVAILABLE;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private CouponTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public void use(Long orderId) {
        if (this.status != CouponStatus.AVAILABLE) {
            throw new IllegalStateException("사용 불가능한 쿠폰입니다.");
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }

        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    public void expire() {
        if (this.status == CouponStatus.AVAILABLE) {
            this.status = CouponStatus.EXPIRED;
        }
    }

    public boolean isAvailable() {
        return this.status == CouponStatus.AVAILABLE
                && LocalDateTime.now().isBefore(this.expiresAt);
    }

    public int calculateDiscount(int orderAmount) {
        if (!isAvailable()) {
            return 0;
        }
        return template.calculateDiscount(orderAmount);
    }
}
