package com.jy.shoppy.domain.category.controller;

import com.jy.shoppy.domain.category.service.CategoryService;
import com.jy.shoppy.domain.category.dto.CategoryResponse;
import com.jy.shoppy.domain.category.dto.CategoryTreeResponse;
import com.jy.shoppy.domain.category.dto.CreateCategoryRequest;
import com.jy.shoppy.domain.category.dto.UpdateCategoryRequest;
import com.jy.shoppy.global.response.ApiResponse;
import com.jy.shoppy.global.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "카테고리 등록 API",
            description = "새로운 카테고리를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@RequestBody @Valid CreateCategoryRequest req) {
        Long id = categoryService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(id, HttpStatus.CREATED));
    }

    @Operation(
            summary = "카테고리 수정 API",
            description = "카테고리 ID로 카테고리를 수정합니다."
    )
    @PutMapping
    public ResponseEntity<ApiResponse<Long>> update(@RequestBody @Valid UpdateCategoryRequest req) {
        Long id = categoryService.update(req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(id, HttpStatus.OK));
    }

    @Operation(
            summary = "카테고리 전체 조회 API",
            description = "카테고리를 전체 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(), HttpStatus.OK));
    }

    @Operation(
            summary = "카테고리 전체 트리 조회 API",
            description = "카테고리를 전체 트리 조회합니다."
    )
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getAllCategoriesAsTree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryTree(), HttpStatus.OK));
    }

    @GetMapping("/caches")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAllCategoriesByCaches(HttpSession session) {
        final String CATEGORY_CACHE_KEY = "cachedCategoriesJson";

        String cachedCategoriesJson = (String) session.getAttribute(CATEGORY_CACHE_KEY);

        if (cachedCategoriesJson == null) {
            List<CategoryResponse> freshCategories = categoryService.getAllCategories();
            session.setAttribute(CATEGORY_CACHE_KEY, JsonUtil.toJson(freshCategories));
            return ResponseEntity.ok(ApiResponse.success(freshCategories, HttpStatus.OK));
        }

        List<CategoryResponse> cachedCategories = JsonUtil.fromJsonList(
                cachedCategoriesJson, CategoryResponse.class
        );
        return ResponseEntity.ok(ApiResponse.success(cachedCategories, HttpStatus.OK));
    }

    // 카테고리 별 최다 판매 순위 Top 10 조회

    @Operation(
            summary = "카테고리 삭제 API",
            description = "하위 카테고리가 없는 경우에만 카테고리를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.deleteById(id);
    }
}

