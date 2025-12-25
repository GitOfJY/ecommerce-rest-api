package com.jy.shoppy.domain.user.repository;

import com.jy.shoppy.domain.user.entity.UserGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserGradeRepository extends JpaRepository<UserGrade, Long> {
    /**
     * 총 구매금액에 해당하는 최고 등급 조회
     * sortOrder가 높을수록 상위 등급
     */
    @Query("SELECT ug FROM UserGrade ug " +
            "WHERE ug.minPurchaseAmount <= :totalAmount " +
            "ORDER BY ug.sortOrder DESC, ug.minPurchaseAmount DESC " +
            "LIMIT 1")
    Optional<UserGrade> findHighestEligibleGrade(@Param("totalAmount") BigDecimal totalAmount);

    Optional<UserGrade> findByName(String name);
}
