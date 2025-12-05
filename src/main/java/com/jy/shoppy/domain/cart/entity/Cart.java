package com.jy.shoppy.domain.cart.entity;

import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 1:1
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Cart 1:N CartProduct
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartProduct> cartProducts = new ArrayList<>();

    public void addProduct(Product product, int quantity) {
        CartProduct cartProduct = CartProduct.builder()
                .cart(this)
                .product(product)
                .quantity(quantity)
                .build();
        this.cartProducts.add(cartProduct);
    }
}
