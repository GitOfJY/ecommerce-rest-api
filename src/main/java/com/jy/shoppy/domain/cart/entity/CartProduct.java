package com.jy.shoppy.domain.cart.entity;

import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
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

    @Column(name = "selected_color", length = 50)
    private String selectedColor;

    @Column(name = "selected_size", length = 20)
    private String selectedSize;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int quantity = 1;

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void updateOptions(String selectedColor, String selectedSize, int quantity) {
        this.selectedColor = selectedColor;
        this.selectedSize = selectedSize;
        if (quantity <= 0) {
            throw new ServiceException(ServiceExceptionCode.INVALID_QUANTITY);
        }
        this.quantity = quantity;
    }

    public static CartProduct createCartProduct(Cart cart, Product product, String color, String size, int quantity) {
        CartProduct cartProduct = CartProduct.builder()
                .cart(cart)
                .product(product)
                .selectedColor(color)
                .selectedSize(size)
                .quantity(quantity)
                .build();
        return cartProduct;
    }
}
