package com.jy.shoppy.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "이메일 찾기 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginIdRequest {
    @Schema(description = "사용자 이름", example = "test")
    @NotBlank(message = "이름은 필수입니다.")
    String username;

    @Schema(description = "휴대폰 번호", example = "01000000000")
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    String phone;
}
