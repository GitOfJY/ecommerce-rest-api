package com.jy.shoppy.domain.order.repository;

import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderProducts op " +
            "LEFT JOIN FETCH op.product " +
            "WHERE o.id = :orderId AND o.user.id = :userId"
    )
    Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId,
                                    @Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderProducts op " +
            "LEFT JOIN FETCH op.product " +
            "WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderProducts op " +
            "LEFT JOIN FETCH op.product " +
            "WHERE o.id = :orderId"
    )
    Optional<Order> findByIdWithProducts(@Param("orderId") Long orderId);

    boolean existsByUserId(Long userId);
}
