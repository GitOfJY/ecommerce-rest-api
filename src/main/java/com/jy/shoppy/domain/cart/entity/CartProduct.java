package com.jy.shoppy.domain.cart.entity;

import com.jy.shoppy.domain.prodcut.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int quantity;

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static CartProduct createCartProduct(Cart cart, Product product, int quantity) {
        CartProduct  cartProduct = CartProduct.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
        return cartProduct;
    }
}
