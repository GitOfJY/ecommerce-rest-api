package com.jy.shoppy.domain.guest.entity;

import com.jy.shoppy.domain.order.dto.CreateGuestOrderRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String passwordHash;

    // 주문자 정보
    @Column(nullable = false, length = 100)
    private String name;

    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    // 배송지 정보 (Address 테이블 구조와 동일)
    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private String street;

    private String detail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Guest createGuest(CreateGuestOrderRequest req, String passwordHash) {
        return Guest.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordHash)
                .zipcode(req.getZipCode())
                .city(req.getCity())
                .street(req.getStreet())
                .detail(req.getDetail())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 전체 주소 조회
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append("[").append(zipcode).append("] ");
        address.append(city).append(" ");
        address.append(street);
        if (detail != null && !detail.isBlank()) {
            address.append(" ").append(detail);
        }
        return address.toString();
    }
}
