package com.jy.shoppy.domain.point.repository;

import com.jy.shoppy.domain.point.entity.PointHistory;
import com.jy.shoppy.domain.point.entity.type.PointType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    /**
     * 사용자별 적립금 내역 조회 (최신순)
     */
    List<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 주문별 적립금 내역 조회
     */
    List<PointHistory> findByOrderId(Long orderId);

    /**
     * 특정 주문의 특정 타입 내역 조회
     */
    Optional<PointHistory> findByOrderIdAndPointType(Long orderId, PointType pointType);
}
