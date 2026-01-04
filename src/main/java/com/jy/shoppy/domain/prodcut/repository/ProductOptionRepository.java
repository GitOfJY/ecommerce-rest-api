package com.jy.shoppy.domain.prodcut.repository;

import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    // 특정 상품의 모든 옵션 조회
    List<ProductOption> findByProductId(Long productId);

    /**
     * 상품 ID, 색상, 사이즈로 옵션 조회
     */
    @Query("""
        SELECT po FROM ProductOption po 
        WHERE po.product.id = :productId 
        AND (:color IS NULL OR po.color = :color)
        AND (:size IS NULL OR po.size = :size)
    """)
    Optional<ProductOption> findByProductIdAndColorAndSize(@Param("productId") Long productId, @Param("color") String color, @Param("size") String size);
}
