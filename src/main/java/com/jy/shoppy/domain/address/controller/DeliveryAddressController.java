package com.jy.shoppy.domain.address.controller;

import com.jy.shoppy.domain.address.dto.DeliveryAddressRequest;
import com.jy.shoppy.domain.address.dto.DeliveryAddressResponse;
import com.jy.shoppy.domain.address.service.DeliveryAddressService;
import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Delivery Address", description = "배송지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery-addresses")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @Operation(
            summary = "배송지 등록 API",
            description = "새로운 배송지를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryAddressResponse>> addAddress(
            @AuthenticationPrincipal Account account,
            @RequestBody @Valid DeliveryAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(deliveryAddressService.addAddress(account, request), HttpStatus.CREATED));
    }

    @Operation(
            summary = "배송지 목록 조회 API",
            description = "내 배송지 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryAddressResponse>>> getAddresses(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(deliveryAddressService.getAddresses(account), HttpStatus.OK));
    }

    @Operation(
            summary = "기본 배송지 변경 API",
            description = "기본 배송지를 변경합니다."
    )
    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<Void>> updateDefault(
            @AuthenticationPrincipal Account account,
            @PathVariable Long id) {
        deliveryAddressService.updateDefault(account, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
            summary = "배송지 삭제 API",
            description = "배송지를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal Account account,
            @PathVariable Long id) {
        deliveryAddressService.deleteAddress(account, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
