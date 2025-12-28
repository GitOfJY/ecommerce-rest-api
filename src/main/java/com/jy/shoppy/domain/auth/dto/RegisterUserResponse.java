package com.jy.shoppy.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jy.shoppy.domain.user.entity.type.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterUserResponse {
    @Schema(description = "사용자 ID")
    private Long id;

    @Schema(description = "사용자 이름")
    private String username;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "회원 등급 (관리자는 null)")
    private String gradeName;

    @Schema(description = "할인율 (관리자는 null)")
    private BigDecimal discountRate;

    @Schema(description = "계정 상태")
    private UserStatus status;
}