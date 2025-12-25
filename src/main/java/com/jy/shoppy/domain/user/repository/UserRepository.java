package com.jy.shoppy.domain.user.repository;

import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.type.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u " +
            "   JOIN FETCH u.role " +
            "   JOIN FETCH u.userGrade " +
            "   WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "   WHERE u.username = :username " +
            "   and u.phone = :phone")
    Optional<User> findByUsernameAndPhone(
            @Param("username") String username,
            @Param("phone") String phone);

    @Query("SELECT u FROM User u " +
            "WHERE u.username = :username " +
            "AND (u.email = :email OR u.phone = :phone)")
    Optional<User> findByEmailOrPhoneWithName(
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("username") String username);

    List<User> findAllByStatusNot(UserStatus status);
}
