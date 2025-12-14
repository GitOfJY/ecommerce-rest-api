package com.jy.shoppy.domain.order.controller;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.order.service.OrderService;
import com.jy.shoppy.domain.order.dto.CreateOrderRequest;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.global.response.ApiResponse;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Order", description = "사용자 주문 관리 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "회원 주문 등록 API",
            description = "새로운 회원 주문을 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @AuthenticationPrincipal Account account,
            @RequestBody @Valid CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.create(account, req), HttpStatus.CREATED));
    }

    @Operation(
            summary = "비회원 주문 등록 API",
            description = "새로운 비회원 주문을 등록합니다."
    )
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<OrderResponse>> createGuestOrder(
            @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.createGuestOrder(req)));
    }

    @Operation(
            summary = "주문 상세 조회 API",
            description = "사용자 ID와 주문 ID로 단일 주문 조회 및 관련 상품 정보 반환합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOne(
            @AuthenticationPrincipal Account account,
            @RequestParam("id") Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.findMyOrderById(account, orderId), HttpStatus.OK));
    }

    @Operation(
            summary = "주문 전체 조회 API",
            description = "사용자 ID로 전체 주문 조회 및 관련 상품 정보 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.findMyOrders(account), HttpStatus.OK));
    }

    @Operation(
            summary = "주문 취소 API",
            description = "주문 ID로 취소합니다." +
                    "(pending 상태의 주문만 취소 가능)"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> delete(@AuthenticationPrincipal Account account,
                                                             @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelMyOrder(account, id)));
    }

    @Operation(
            summary = "주문 검색 API",
            description = "주문을 검색합니다." +
                    "(검색 및 필터링 조건: 주문 상태, 날짜)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> searchOrders(
            @AuthenticationPrincipal Account account,
            SearchOrderCond cond,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.searchOrdersPage(account, cond, pageable), HttpStatus.OK));
    }
}
