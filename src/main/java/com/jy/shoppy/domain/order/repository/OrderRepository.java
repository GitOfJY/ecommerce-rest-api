package com.jy.shoppy.domain.order.repository;

import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    public boolean existsByOrderProductsProductIdAndStatus(Long productId, OrderStatus status);
}
