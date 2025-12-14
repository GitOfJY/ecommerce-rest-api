package com.jy.shoppy.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class RegisterUserRequest {
    @NotBlank(message = "이름은 필수입니다.")
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String passwordHash;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private Long roleId;

    public Long getRoleId() {
        return roleId != null ? roleId : 1L;
    }
}
