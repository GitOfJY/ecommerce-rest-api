package com.jy.shoppy.domain.cart.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.cart.dto.*;
import com.jy.shoppy.domain.cart.service.CartService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
            description = "상품을 장바구니에 추가합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @AuthenticationPrincipal Account account,
            @RequestBody CartProductRequest request,
            HttpSession session) {
        cartService.addToCart(account, request, session);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(
            summary = "장바구니 목록 조회 API",
            description = "장바구니 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartProductResponse>>> getAll(
            @AuthenticationPrincipal Account account,
            HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(cartService.findAllByUserId(account, session), HttpStatus.OK));
    }

    @Operation(
            summary = "장바구니 옵션 수정 API",
            description = "장바구니 옵션(컬러/사이즈/수량)을 수정합니다."
    )
    @PutMapping("/cart-items/{cartProductId}/option")
    public ResponseEntity<ApiResponse<CartProductResponse>> updateOption(
            @AuthenticationPrincipal Account account,
            @PathVariable Long cartProductId,
            @RequestBody @Valid UpdateCartOptionRequest request,
            HttpSession session
    ) {
        cartService.updateOption(account, cartProductId, request, session);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(
            summary = "장바구니 상품 삭제 API",
            description = "장바구니 상품을 삭제합니다."
    )
    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> deleteCart(
            @AuthenticationPrincipal Account account,
            @RequestBody @Valid DeleteCartProductRequest request,
            HttpSession session) {
        cartService.deleteCartByIds(account, request, session);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(
            summary = "장바구니 상품 전체 삭제 API",
            description = "장바구니 상품 전체 삭제합니다."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> clearCart(
            @AuthenticationPrincipal Account account,
            HttpSession session) {
        cartService.clearCart(account, session);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
