package com.jy.shoppy.domain.cart.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.cart.dto.CartProductRequest;
import com.jy.shoppy.domain.cart.dto.CartProductResponse;
import com.jy.shoppy.domain.cart.entity.CartProduct;
import com.jy.shoppy.domain.cart.service.CartService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {
    private final CartService cartService;

    @Operation(
            summary = "장바구니 상품 추가 API",
            description = "상품을 장바구니에 추가합니다. (회원/비회원 가능)"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @AuthenticationPrincipal Account account,
            @RequestBody CartProductRequest request,
            HttpSession session) {
        Long userId = (account != null) ? account.getAccountId() : null;
        cartService.addToCart(userId, request, session);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(
            summary = "장바구니 목록 조회 API",
            description = "사용자를 기반으로 장바구니 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartProductResponse>>> getAll(Long userId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(cartService.findAllByUserId(userId, session), HttpStatus.OK));
    }
}
