package com.jy.shoppy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cart 1:N
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Product N:1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    public void changeQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        this.quantity = quantity;
    }

    public void addQuantity(int delta) {
        int newQty = this.quantity + delta;
        if (newQty <= 0) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        this.quantity = newQty;
    }
}
