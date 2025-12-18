package com.jy.shoppy.domain.address.entity;

import com.jy.shoppy.domain.address.dto.DeliveryAddressRequest;
import com.jy.shoppy.domain.order.dto.CreateOrderRequest;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_addresses")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
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

    private String alias;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    // 회원 주문용
    public static DeliveryAddress createDeliveryAddress(User user, CreateOrderRequest req) {
        Address address = Address.builder()
                .zipCode(req.getZipCode())
                .city(req.getCity())
                .street(req.getStreet())
                .detail(req.getDetail())
                .build();

        return DeliveryAddress.builder()
                .user(user)
                .address(address)
                .recipientName(req.getRecipientName())
                .recipientPhone(req.getRecipientPhone())
                .recipientEmail(req.getRecipientEmail())
                .alias(null)
                .isDefault(false)
                .build();
    }

    // 비회원 주문용
    public static DeliveryAddress createGuestDeliveryAddress(CreateOrderRequest req) {
        Address address = Address.builder()
                .zipCode(req.getZipCode())
                .city(req.getCity())
                .street(req.getStreet())
                .detail(req.getDetail())
                .build();

        return DeliveryAddress.builder()
                .user(null)
                .address(address)
                .recipientName(req.getRecipientName())
                .recipientPhone(req.getRecipientPhone())
                .recipientEmail(req.getRecipientEmail())
                .alias(null)
                .isDefault(false)
                .build();
    }

    // 배송지 관리용 (회원 전용)
    public static DeliveryAddress createDeliveryAddress(User user, DeliveryAddressRequest req, boolean isDefault) {
        Address address = Address.builder()
                .zipCode(req.getZipCode())
                .city(req.getCity())
                .street(req.getStreet())
                .detail(req.getDetail())
                .build();

        return DeliveryAddress.builder()
                .user(user)
                .address(address)
                .recipientName(req.getRecipientName())
                .recipientPhone(req.getRecipientPhone())
                .recipientEmail(req.getRecipientEmail())
                .alias(req.getAlias())
                .isDefault(isDefault)
                .build();
    }
}
