package com.jy.shoppy.domain.refund.controller;

import com.jy.shoppy.domain.refund.dto.CreateRefundRequest;
import com.jy.shoppy.domain.refund.dto.CreateRefundResponse;
import com.jy.shoppy.domain.refund.dto.RefundResponse;
import com.jy.shoppy.domain.refund.dto.UpdateRefundRequest;
import com.jy.shoppy.domain.refund.entity.type.RefundStatus;
import com.jy.shoppy.domain.refund.service.RefundService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    @Operation(
            summary = "환불 요청 API",
            description = "환불 요청을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CreateRefundResponse>> create(@RequestBody @Valid CreateRefundRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(refundService.create(req), HttpStatus.CREATED));
    }

    @Operation(
            summary = "환불 처리 API",
            description = "환불 요청을 처리합니다."
    )
    @PutMapping("/{refundId}")
    public ResponseEntity<ApiResponse<Long>> update(
            @PathVariable Long refundId,
            @RequestBody @Valid UpdateRefundRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(refundService.updateRefund(refundId, req), HttpStatus.CREATED));
    }

    @Operation(
            summary = "사용자별 환불 조회 API",
            description = "사용자Id로 환불을 조회합니다." +
                    "(필터링 조건: 주문 상태환불 상태(pending, approved, rejected))"
    )
    @GetMapping("/{userId}")
    // TODO @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RefundResponse>>> getUserRefunds(@PathVariable Long userId,
                                                                            @RequestParam(required = false) RefundStatus status,
                                                                            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(refundService.searchRefunds(userId, status, pageable), HttpStatus.OK));
    }

    // TODO 사용자용 API (본인 환불만 조회)
    /*
    * @Operation(summary = "내 환불 목록 조회", description = "로그인한 사용자의 환불 목록을 조회합니다.")
    * @GetMapping("/my")
    * public ResponseEntity<ApiResponse<Page<RefundResponse>>> getMyRefunds(
          @RequestParam(required = false) RefundStatus status,
          Pageable pageable ) {
         // 로그인한 사용자 ID 가져오기 (Spring Security 등)
         Long currentUserId = getCurrentUserId();

        return ResponseEntity.ok(
            ApiResponse.success(refundService.searchRefunds(currentUserId, status, pageable), HttpStatus.OK)
        );
     }
    */
}