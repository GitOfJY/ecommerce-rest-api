package com.jy.shoppy.domain.address.repository;

import com.jy.shoppy.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
