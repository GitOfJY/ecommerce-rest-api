package com.sparta.demo.repository;

import com.sparta.demo.entity.Order;
import com.sparta.demo.entity.type.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    public boolean existsByOrderProductsProductIdAndStatus(Long productId, OrderStatus status);
}
