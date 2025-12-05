package com.jy.shoppy.service.dto;

import lombok.Getter;
import org.wildfly.common.annotation.NotNull;

@Getter
public class UpdateUserRequest {
    @NotNull
    private Long id;

    private String passwordHash;

    private String address;

    private String email;
}
