package com.jy.shoppy.domain.prodcut.repository;

import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    // 색상 × 사이즈 조합으로 찾기
    Optional<ProductOption> findByProductIdAndColorAndSize(Long productId, String color, String size);

    // 특정 상품의 모든 옵션 조회
    List<ProductOption> findByProductId(Long productId);

}
