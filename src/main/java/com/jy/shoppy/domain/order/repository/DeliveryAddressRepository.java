package com.jy.shoppy.domain.order.repository;

import com.jy.shoppy.domain.order.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
}
