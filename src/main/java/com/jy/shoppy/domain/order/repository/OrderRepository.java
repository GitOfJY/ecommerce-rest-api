package com.jy.shoppy.domain.order.repository;

import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.OrderProduct;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.user.entity.User;
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

    boolean existsByOrderProductsProductIdAndStatus(Long productId, OrderStatus status);

    @Query("SELECT o FROM Order o JOIN FETCH o.guest g WHERE o.orderNumber = :orderNumber AND o.guest IS NOT NULL")
    Optional<Order> findGuestOrderByOrderNumber(@Param("orderNumber") String orderNumber);

    @Query("""
        SELECT op FROM OrderProduct op
        WHERE op.id = :orderProductId
    """)
    OrderProduct findOrderProductsById(@Param("orderProductId") Long orderProductId);

    Long user(User user);

    /**
     * 특정 사용자의 리뷰 작성 가능한 주문 상품 목록 조회
     */
    /*
    @Query("""
        SELECT op FROM OrderProduct op
        JOIN FETCH op.order o
        JOIN FETCH op.product p
        LEFT JOIN Review r ON r.orderProduct.id = op.id
        WHERE o.user.id = :userId
          AND o.status = 'COMPLETED'
          AND r.id IS NULL
        ORDER BY o.createdAt DESC
    """)
    List<OrderProduct> findReviewableOrderProducts(@Param("userId") Long userId);
     */
}
