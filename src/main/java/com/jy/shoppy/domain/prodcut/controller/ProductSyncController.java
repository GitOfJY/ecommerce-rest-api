package com.jy.shoppy.domain.prodcut.controller;

import com.jy.shoppy.domain.prodcut.dto.ProductSyncResult;
import com.jy.shoppy.domain.prodcut.service.ProductSyncService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Product Sync", description = "외부 상품 동기화 관리")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
public class ProductSyncController {
    private final ProductSyncService productSyncService;

    /**
     * 수동으로 외부 상품 동기화 실행
     */
    @Operation(summary = "외부 상품 동기화", description = "외부 API에서 상품 데이터를 가져와 동기화합니다.")
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<ProductSyncResult>> syncProducts() {
        log.info("수동 외부 상품 동기화 요청");
        ProductSyncResult result = productSyncService.syncExternalProducts();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
