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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void updateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ServiceException(ServiceExceptionCode.INVALID_QUANTITY);
        }
        this.quantity = quantity;
    }

    public static CartProduct createCartProduct(Cart cart, Product product,  ProductOption option, int quantity) {
        CartProduct  cartProduct = CartProduct.builder()
                .cart(cart)
                .product(product)
                .productOption(option)
                .quantity(quantity)
                .build();
        return cartProduct;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal unitPrice = productOption != null
                ? productOption.getTotalPrice()   // 옵션 가격 포함
                : product.getPrice();             // 기본 가격만
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
