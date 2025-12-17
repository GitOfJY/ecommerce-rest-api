package com.jy.shoppy.domain.address.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "addresses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zipCode;
    private String city;
    private String street;
    private String detail;

    private LocalDateTime createdAt;

    @Builder
    public Address(String zipCode, String city, String street, String detail) {
        this.zipCode = zipCode;
        this.city = city;
        this.street = street;
        this.detail = detail;
        this.createdAt = LocalDateTime.now();
    }

    public String getFullAddress() {
        return String.format("(%s) %s %s %s", zipCode, city, street, detail);
    }
}
