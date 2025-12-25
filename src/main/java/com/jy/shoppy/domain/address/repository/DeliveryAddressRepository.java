package com.jy.shoppy.domain.address.repository;

import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@RequestMapping
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    @Query("SELECT d FROM DeliveryAddress d WHERE d.user.id = :userId AND d.isTemporary = false ORDER BY d.isDefault DESC, d.createdAt DESC")
    List<DeliveryAddress> findByUserIdOrderByIsDefaultDesc(@Param("userId") Long userId);

    // 또는 메서드 네이밍으로
    List<DeliveryAddress> findByUserIdAndIsTemporaryFalseOrderByIsDefaultDesc(Long userId);

    Optional<DeliveryAddress> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<DeliveryAddress> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DeliveryAddress d SET d.isDefault = false WHERE d.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DeliveryAddress d SET d.isDefault = true WHERE d.id = :id")
    void updateDefaultTrue(@Param("id") Long id);

    void deleteAllByUserId(Long userId);
}
