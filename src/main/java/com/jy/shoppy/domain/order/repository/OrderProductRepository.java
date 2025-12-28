package com.jy.shoppy.domain.order.repository;

import com.jy.shoppy.domain.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    Optional<OrderProduct> findById(Long orderProductId);
}
