package com.jy.shoppy.domain.point.entity;

import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.point.entity.type.PointType;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 20)
    private PointType pointType;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 적립금 내역 생성
     */
    public static PointHistory create(User user, Order order, PointType pointType, Integer amount, String description, LocalDateTime expiresAt) {
        return PointHistory.builder()
                .user(user)
                .order(order)
                .pointType(pointType)
                .amount(amount)
                .balanceAfter(user.getPoints())  // 변동 후 잔액
                .description(description)
                .expiresAt(expiresAt)
                .build();
    }
}
