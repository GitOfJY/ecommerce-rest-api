package com.jy.shoppy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "사용자 정보 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    @Schema(description = "사용자 ID", example = "1")
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long id;

    @Schema(description = "새 이메일", example = "newemail@example.com")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email;

    @Schema(description = "새 비밀번호 (해싱된 값)", example = "$2a$10$...")
    String passwordHash;
}
