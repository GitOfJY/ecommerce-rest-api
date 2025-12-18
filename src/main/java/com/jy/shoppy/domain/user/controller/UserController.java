package com.jy.shoppy.domain.user.controller;

import com.jy.shoppy.domain.user.dto.LoginIdRequest;
import com.jy.shoppy.domain.user.dto.LoginPasswordRequest;
import com.jy.shoppy.domain.user.service.UserService;
import com.jy.shoppy.domain.user.dto.UpdateUserRequest;
import com.jy.shoppy.domain.user.dto.UserResponse;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Public API", description = "사용자 공개 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "사용자 ID(email) 조회 API",
            description = "사용자의 이름, 휴대폰으로 ID(email)를 조회합니다."
    )
    @PostMapping("/find-email")
    public ResponseEntity<ApiResponse<String>> findEmail(@RequestBody @Valid LoginIdRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.findEmail(request), HttpStatus.OK));
    }

    @Operation(
            summary = "사용자 비밀번호 임시 비밀번호 발급 API",
            description = "이메일 또는 휴대폰 번호로 비밀번호 임시 비밀번호 발급합니다."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@RequestBody @Valid LoginPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.sendTemporaryPassword(request), HttpStatus.OK));
    }
}
