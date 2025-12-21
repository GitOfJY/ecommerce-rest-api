package com.jy.shoppy.domain.cart.repository;

import com.jy.shoppy.domain.cart.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    @Query("SELECT cp FROM CartProduct cp " +
            "WHERE cp.cart.id = :cartId " +
            "AND cp.product.id = :productId " +
            "AND (:color IS NULL OR cp.selectedColor = :color) " +
            "AND (:size IS NULL OR cp.selectedSize = :size)")
    Optional<CartProduct> findByCartIdAndProductIdAndOptions(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId,
            @Param("color") String color,
            @Param("size") String size
    );

    @Modifying
    @Query("DELETE FROM CartProduct cp " +
            "WHERE cp.id IN :ids " +
            "AND cp.cart.user.id = :userId")
    int deleteByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CartProduct cp WHERE cp.cart.id = :cartId")
    int deleteByCartId(@Param("cartId") Long cartId);
}
