package com.jy.shoppy.domain.order.repository;

import com.jy.shoppy.domain.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    Optional<OrderProduct> findById(Long orderProductId);

    /**
     * 리뷰 작성 가능한 주문 상품 목록 조회
     * - 주문 상태가 COMPLETED
     * - 아직 리뷰를 작성하지 않은 상품
     */
    @Query("""
        SELECT op FROM OrderProduct op
        JOIN FETCH op.order o
        JOIN FETCH op.product p
        LEFT JOIN Review r ON r.orderProduct.id = op.id
        WHERE o.user.id = :userId
          AND o.status = 'COMPLETED'
          AND r.id IS NULL
        ORDER BY o.orderDate DESC
    """)
    List<OrderProduct> findReviewableOrderProducts(@Param("userId") Long userId);

}
