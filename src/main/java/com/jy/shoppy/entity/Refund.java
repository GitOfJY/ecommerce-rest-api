package com.jy.shoppy.entity;

import com.jy.shoppy.common.ServiceException;
import com.jy.shoppy.common.ServiceExceptionCode;
import com.jy.shoppy.entity.type.OrderStatus;
import com.jy.shoppy.entity.type.RefundStatus;
import com.jy.shoppy.service.dto.CreateRefundRequest;
import com.jy.shoppy.service.dto.UpdateRefundRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, length = 500)
    private String reason;

    // 환불 요청 시간
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 환불 상태 변경 시간
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 환불 처리 완료 시간
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(name = "refund_amount", nullable = false)
    private BigDecimal refundAmount;

    public static Refund createRefund(CreateRefundRequest req, Order order) {
        // userId 확인
        Long findUser = order.getUser().getId();
        if (!findUser.equals(req.getUserId())) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER);
        }

        // 주문상태 확인
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_REQUEST_REFUND_NOT_COMPLETED);
        }

        Refund refund = Refund.builder()
                .user(order.getUser())
                .order(order)
                .reason(req.getReason())
                .refundAmount(getRefundAmount(order))
                .createdAt(LocalDateTime.now())
                .status(RefundStatus.PENDING)
                .build();

        return  refund;
    }

    public static BigDecimal getRefundAmount(Order order) {
        return order.getTotalPrice();
    }

    public void updateRefund(UpdateRefundRequest req, User admin) {
        // 환불요청 승인/거절 선택
        this.status = req.getRefundStatus();
        this.processedAt = LocalDateTime.now();
        this.processedBy = admin;
    }
}
