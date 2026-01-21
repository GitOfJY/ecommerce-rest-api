package com.jy.shoppy.domain.prodcut.repository;

import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.type.ProductSource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // 재고 차감 시 동시성 안전을 위해 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    Optional<Product> findById(@Param("id") Long id);

    // ========== 외부 상품 동기화용 메서드 추가 ==========
    Optional<Product> findByExternalProductId(String externalProductId);

    @Query("SELECT p FROM Product p WHERE p.externalProductId IN :externalIds")
    List<Product> findAllByExternalProductIdIn(@Param("externalIds") List<String> externalIds);

    // 동기화 누락된 외부 상품 조회 (삭제 감지용)
    @Query("SELECT p FROM Product p WHERE p.source = 'EXTERNAL' " +
            "AND (p.lastSyncedAt IS NULL OR p.lastSyncedAt < :threshold)")
    List<Product> findStaleExternalProducts(@Param("threshold") LocalDateTime threshold);

    /**
     * 상품 출처별 조회
     */
    List<Product> findAllBySource(ProductSource source);

    /**
     * 주문 가능한 상품만 조회
     */
    List<Product> findAllByIsOrderableTrue();
}

