package com.jy.shoppy.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jy.shoppy.domain.user.entity.type.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    @Schema(description = "사용자 ID")
    Long userId;

    @Schema(description = "사용자 이름")
    String username;

    @Schema(description = "이메일")
    String email;

    @Schema(description = "권한")
    String role;

    @Schema(description = "회원 등급 (관리자는 null)")
    String gradeName;

    @Schema(description = "할인율 (관리자는 null)")
    BigDecimal discountRate;

    @Schema(description = "계정 상태")
    UserStatus status;
}
