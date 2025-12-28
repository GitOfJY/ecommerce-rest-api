package com.jy.shoppy.domain.review.repository;

import com.jy.shoppy.domain.review.entity.Review;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 상품의 평균 평점 조회
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.product.id = :productId")
    Optional<BigDecimal> findAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * 특정 상품의 리뷰 개수 조회
     */
    Long countByProductId(Long productId);

    List<Review>  findByUserId(Long userId);

    List<Review> findByProductId(Long productId);

    boolean existsByOrderProductId(Long orderProductId);
}
