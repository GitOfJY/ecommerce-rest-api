package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.CouponProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponProductRepository extends JpaRepository<CouponProduct, Long> {
    /**
     * 쿠폰의 적용 가능 상품 목록 조회
     */
    @Query("SELECT cp FROM CouponProduct cp " +
            "JOIN FETCH cp.product " +
            "WHERE cp.coupon.id = :couponId")
    List<CouponProduct> findByCouponIdWithProduct(@Param("couponId") Long couponId);

    /**
     * 쿠폰-상품 존재 여부 확인
     */
    boolean existsByCouponIdAndProductId(Long couponId, Long productId);

    /**
     * 쿠폰-상품 삭제
     */
    void deleteByCouponIdAndProductId(Long couponId, Long productId);
}
