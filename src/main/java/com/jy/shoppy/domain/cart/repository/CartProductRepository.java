package com.jy.shoppy.domain.cart.repository;

import com.jy.shoppy.domain.cart.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long productId);
}
