package com.jy.shoppy.domain.user.entity;

import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.user.dto.UpdateUserRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 유저는 반드시 1개의 Role을 가진다
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    // @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    // private Cart cart;

    public void updateUser(UpdateUserRequest req) {
        this.passwordHash = req.getPasswordHash();
        this.address = req.getAddress();
        this.email = req.getEmail();
        this.updatedAt = LocalDateTime.now();
    }

    public static User registerUser(RegisterUserRequest dto, String encodedPassword) {
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(encodedPassword)
                .address(dto.getAddress())
                .role(Role.ref(dto.getRoleId()))
                .build();
    }
}