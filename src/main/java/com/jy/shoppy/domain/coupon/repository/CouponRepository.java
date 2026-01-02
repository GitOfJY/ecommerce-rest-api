package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByName (String couponName);
}
