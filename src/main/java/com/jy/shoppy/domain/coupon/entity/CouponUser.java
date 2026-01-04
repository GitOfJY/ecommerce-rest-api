package com.jy.shoppy.domain.coupon.entity;

import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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
            throw new ServiceException(ServiceExceptionCode.ALREADY_REGISTERED_COUPON);
        }
        if (!isAvailable()) {
            throw new ServiceException(ServiceExceptionCode.CAN_NOT_REGISTER_COUPON);
        }

        this.user = user;
        this.status = CouponStatus.ISSUED;
    }

    /**
     * 쿠폰 사용
     */
    public void use(Long orderId) {
        if (this.status != CouponStatus.AVAILABLE) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_USE_COUPON);
        }
        if (LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = CouponStatus.EXPIRED;
            throw new ServiceException(ServiceExceptionCode.EXPIRED_COUPON);
        }
        if (this.user == null) {
            throw new ServiceException(ServiceExceptionCode.NOT_ASSIGNED_COUPON);
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
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 쿠폰 사용 가능 여부 (status + 만료 확인)
     */
    public boolean isAvailable() {
        return this.status == CouponStatus.ISSUED
                && LocalDateTime.now().isBefore(this.expiresAt);
    }

    /**
     * 할인 금액 계산
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isAvailable()) {
            return BigDecimal.ZERO;
        }
        return coupon.calculateDiscount(orderAmount);
    }

    /**
     * 특정 사용자의 쿠폰인지 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    /**
     * 쿠폰 복구 (주문 취소 시)
     */
    public void restore() {
        this.status = CouponStatus.ISSUED;
        this.usedAt = null;
        this.orderId = null;
    }
}