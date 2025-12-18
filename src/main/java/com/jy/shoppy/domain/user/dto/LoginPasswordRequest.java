package com.jy.shoppy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Schema(description = "비밀번호 찾기 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginPasswordRequest {
    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    String username;

    @Schema(description = "이메일", example = "hong@example.com")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email;

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    String phone;

    @AssertTrue(message = "이메일 또는 휴대폰 번호 중 하나는 필수입니다.")
    public boolean isEitherEmailOrPhoneProvided() {
        return (email != null && !email.isBlank()) ||
                (phone != null && !phone.isBlank());
    }
}
