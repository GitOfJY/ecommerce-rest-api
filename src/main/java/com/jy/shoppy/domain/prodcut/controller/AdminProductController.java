package com.jy.shoppy.domain.prodcut.controller;

import com.jy.shoppy.domain.prodcut.dto.CreateProductRequest;
import com.jy.shoppy.domain.prodcut.dto.ProductResponse;
import com.jy.shoppy.domain.prodcut.dto.UpdateProductRequest;
import com.jy.shoppy.domain.prodcut.service.ProductService;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Product", description = "관리자 상품 관리 API")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final ProductService productService;

    @Operation(
            summary = "[관리자] 상품 등록 API",
            description = "새로운 상품을 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@RequestBody @Valid CreateProductRequest req) {
        ProductResponse response = productService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, HttpStatus.CREATED));
    }

    @Operation(
            summary = "[관리자] 상품 수정 API",
            description = "상품 정보를 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProductRequest req) {
        return ResponseEntity.ok(ApiResponse.success(productService.update(id, req), HttpStatus.OK));
    }

    @Operation(
            summary = "[관리자] 상품 삭제 API",
            description = "상품을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.deleteProduct(id)));
    }
}
