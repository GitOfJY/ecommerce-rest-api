package com.jy.shoppy.domain.order.controller;

import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import com.jy.shoppy.domain.order.service.AdminOrderService;
import com.jy.shoppy.domain.order.service.OrderService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Order", description = "관리자 주문 관리 API")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final AdminOrderService orderService;

    @Operation(
            summary = "[관리자] 주문 상세 조회 API",
            description = "모든 주문의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.findById(id), HttpStatus.OK));
    }

    @Operation(
            summary = "[관리자] 전체 주문 조회 API",
            description = "모든 주문을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.findAll(), HttpStatus.OK));
    }

    @Operation(
            summary = "[관리자] 주문 완료 처리 API",
            description = "주문을 완료 상태로 변경합니다. 회원 주문인 경우 구매금액이 누적되고 등급이 자동으로 승급됩니다."
    )
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.complete(id), HttpStatus.OK));
    }

    @Operation(
            summary = "[관리자] 주문 취소 API",
            description = "모든 주문을 취소할 수 있습니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.cancel(id)));
    }

    @Operation(
            summary = "[관리자] 주문 검색 API",
            description = "모든 주문을 검색합니다. (검색 조건: 주문 상태, 날짜, 사용자 등)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> searchOrders(
            SearchOrderCond cond,
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.searchOrdersPage(cond, pageable), HttpStatus.OK));
    }
}
