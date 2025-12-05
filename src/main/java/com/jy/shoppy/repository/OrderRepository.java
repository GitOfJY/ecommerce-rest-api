package com.jy.shoppy.repository;

import com.jy.shoppy.entity.Order;
import com.jy.shoppy.entity.type.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    public boolean existsByOrderProductsProductIdAndStatus(Long productId, OrderStatus status);
}
