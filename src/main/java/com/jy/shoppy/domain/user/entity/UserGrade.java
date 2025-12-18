package com.jy.shoppy.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    public static UserGrade ref(Long id) {
        UserGrade grade = new UserGrade();
        grade.id = id;
        return grade;
    }
}
