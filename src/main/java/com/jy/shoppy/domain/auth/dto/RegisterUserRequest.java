package com.jy.shoppy.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Schema(description = "회원가입 요청")
public class RegisterUserRequest {
    @Schema(description = "사용자 이름", example = "test")
    @NotBlank(message = "이름은 필수입니다.")
    private String username;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phone;

    @Schema(description = "비밀번호 (8~20자, 대/소문자/숫자/특수문자 각 1개 이상)", example = "Password1!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 8~20자, 대문자/소문자/숫자/특수문자를 각 1개 이상 포함해야 합니다."
    )
    private String password;

    @Schema(description = "역할 ID (미입력 시 일반회원: 1, 관리자 : 2)", example = "1", nullable = true)
    private Long roleId;

    public Long getRoleId() {
        return roleId != null ? roleId : 1L;
    }
}
