package com.jy.shoppy.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "user_grade")
public class UserGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int minPurchaseAmount;
    private BigDecimal pointRate;
    private BigDecimal discountRate;
    private Integer freeShippingThreshold;
    private int sortOrder;
}
