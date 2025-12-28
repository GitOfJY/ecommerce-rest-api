package com.jy.shoppy.domain.auth.controller;


import com.jy.shoppy.domain.auth.dto.LoginRequest;
import com.jy.shoppy.domain.auth.dto.LoginResponse;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "사용자 등록 API",
            description = "새로운 사용자를 등록합니다."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.register(request), HttpStatus.CREATED));
    }

    @Operation(
            summary = "로그인 API",
            description = "사용자가 로그인합니다." +
                    "(Spring Security가 처리, 이 엔드포인트는 직접 호출되지 않음)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return null;
    }

    @Operation(
            summary = "로그아웃 API",
            description = "사용자가 로그아웃합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(
            summary = "로그인 상태 확인 API",
            description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<LoginResponse>> checkStatus(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(authService.getLoginInfo(authentication)));
    }
}
