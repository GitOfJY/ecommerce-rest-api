package com.jy.shoppy.domain.guest.repository;

import com.jy.shoppy.domain.guest.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}
