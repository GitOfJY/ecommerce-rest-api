package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.CouponUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository extends JpaRepository<CouponUser, Long> {
    boolean existsByCode(String code);

    /**
     * 쿠폰 코드로 조회 (Coupon과 조인)
     */
    @Query("SELECT cu FROM CouponUser cu JOIN FETCH cu.coupon WHERE cu.code = :code")
    Optional<CouponUser> findByCodeWithCoupon(@Param("code") String code);

    /**
     * 사용자가 해당 쿠폰을 이미 등록했는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(cu) > 0 THEN true ELSE false END " +
            "FROM CouponUser cu " +
            "WHERE cu.user.id = :userId " +
            "AND cu.coupon.id = :couponId " +
            "AND cu.status IN ('ISSUED', 'USED')")
    boolean existsByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 사용자 ID로 조회 (Coupon과 조인)
     */
    @Query("SELECT cu FROM CouponUser cu JOIN FETCH cu.coupon WHERE cu.user.id = :userId")
    List<CouponUser> findByUserIdWithCoupon(@Param("userId") Long userId);

    /**
     * 쿠폰 코드로 조회 (비관적 락)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cu FROM CouponUser cu JOIN FETCH cu.coupon WHERE cu.code = :code")
    Optional<CouponUser> findByCodeWithCouponForUpdate(@Param("code") String code);

    /**
     * ID로 조회 (Coupon과 조인)
     */
    @Query("SELECT cu FROM CouponUser cu JOIN FETCH cu.coupon WHERE cu.id = :id")
    Optional<CouponUser> findByIdWithCoupon(@Param("id") Long id);

}