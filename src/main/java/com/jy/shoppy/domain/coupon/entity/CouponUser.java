package com.jy.shoppy.domain.coupon.entity;

import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    Coupon coupon;

    @Column(nullable = false, length = 50, unique = true)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    CouponStatus status = CouponStatus.AVAILABLE;

    @Column(name = "issued_at")
    LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "used_at")
    LocalDateTime usedAt;

    @Column(name = "order_id")
    Long orderId;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }

    /**
     * 사용자에게 쿠폰 할당
     */
    public void assignToUser(User user) {
        if (this.user != null) {
            throw new IllegalStateException("이미 할당된 쿠폰입니다.");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("할당할 수 없는 쿠폰입니다.");
        }
        this.user = user;
    }

    /**
     * 쿠폰 사용
     */
    public void use(Long orderId) {
        if (this.status != CouponStatus.AVAILABLE) {
            throw new IllegalStateException("사용 불가능한 쿠폰입니다. 현재 상태: " + this.status);
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = CouponStatus.EXPIRED;
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        if (this.user == null) {
            throw new IllegalStateException("사용자가 할당되지 않은 쿠폰입니다.");
        }

        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    /**
     * 쿠폰 만료 처리
     */
    public void expire() {
        if (this.status == CouponStatus.AVAILABLE) {
            this.status = CouponStatus.EXPIRED;
        }
    }

    /**
     * 쿠폰 사용 가능 여부
     */
    public boolean isAvailable() {
        return this.status == CouponStatus.AVAILABLE
                && LocalDateTime.now().isBefore(this.expiresAt);
    }

    /**
     * 할인 금액 계산
     */
    public int calculateDiscount(int orderAmount) {
        if (!isAvailable()) {
            return 0;
        }
        return coupon.calculateDiscount(orderAmount);
    }

    /**
     * 특정 사용자의 쿠폰인지 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }
}