package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByName (String couponName);

    Optional<Coupon> findByName(String couponName);
}
