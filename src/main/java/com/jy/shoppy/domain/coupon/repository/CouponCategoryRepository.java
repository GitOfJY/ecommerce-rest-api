package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.CouponCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponCategoryRepository extends JpaRepository<CouponCategory, Long> {
    /**
     * 쿠폰의 적용 가능 카테고리 목록 조회
     */
    @Query("SELECT cc FROM CouponCategory cc " +
            "JOIN FETCH cc.category " +
            "WHERE cc.coupon.id = :couponId")
    List<CouponCategory> findByCouponIdWithCategory(@Param("couponId") Long couponId);

    /**
     * 쿠폰-카테고리 존재 여부 확인
     */
    boolean existsByCouponIdAndCategoryId(Long couponId, Long categoryId);

    /**
     * 쿠폰-카테고리 삭제
     */
    void deleteByCouponIdAndCategoryId(Long couponId, Long categoryId);
}
