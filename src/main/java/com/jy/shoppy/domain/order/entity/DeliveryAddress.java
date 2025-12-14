package com.jy.shoppy.domain.order.entity;

import com.jy.shoppy.domain.order.dto.CreateOrderRequest;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_addresses")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private String address;

    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static DeliveryAddress createDeliveryAddress(User user, CreateOrderRequest req) {
        DeliveryAddress deliveryAddress = DeliveryAddress.builder()
                .user(user)
                .recipientName(req.getRecipientName())
                .recipientEmail(req.getRecipientEmail())
                .recipientPhone(req.getRecipientPhone())
                .address(req.getAddress())
                .build();
        return deliveryAddress;
    }
}
