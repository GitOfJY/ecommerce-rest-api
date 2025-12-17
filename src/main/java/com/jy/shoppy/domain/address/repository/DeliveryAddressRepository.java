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
    List<DeliveryAddress> findByUserIdOrderByIsDefaultDesc(Long userId);

    Optional<DeliveryAddress> findByUserIdAndIsDefaultTrue(Long userId);

    boolean existsByUserId(Long userId);

    @Modifying
    @Query("UPDATE DeliveryAddress d SET d.isDefault = false WHERE d.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE DeliveryAddress d SET d.isDefault = true WHERE d.id = :id")
    void updateDefaultTrue(@Param("id") Long id);
}
