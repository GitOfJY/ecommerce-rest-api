package com.jy.shoppy.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name; // ROLE_USER, ROLE_ADMIN

    private String description;

    @OneToMany(mappedBy = "role")
    private List<User> users = new ArrayList<>();

    public static Role ref(Long id) {
        Role role = new Role();
        role.id = id;
        return role;
    }
}
