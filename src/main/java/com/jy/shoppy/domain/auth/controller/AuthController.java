package com.jy.shoppy.domain.auth.controller;


import com.jy.shoppy.domain.auth.dto.LoginRequest;
import com.jy.shoppy.domain.auth.dto.LoginResponse;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterUserRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, HttpStatus.CREATED));
    }

    @Operation(summary = "로그인 API", description = "실제 처리는 Filter에서 수행")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return null;
    }

    @Operation(
            summary = "로그아웃 API",
            description = "사용자가 로그아웃합니다."
    )
    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(
            summary = "로그인 상태 확인 API",
            description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<LoginResponse>> checkStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER);
        }
        return ResponseEntity.ok(ApiResponse.success(authService.getLoginInfo(authentication), HttpStatus.OK));
    }
}
