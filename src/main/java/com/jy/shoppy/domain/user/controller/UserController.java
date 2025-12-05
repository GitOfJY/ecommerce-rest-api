package com.jy.shoppy.domain.user.controller;

import com.jy.shoppy.domain.user.service.UserService;
import com.jy.shoppy.domain.user.dto.CreateUserRequest;
import com.jy.shoppy.domain.user.dto.UpdateUserRequest;
import com.jy.shoppy.domain.user.dto.UserResponse;
import com.jy.shoppy.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "사용자 등록 API",
            description = "새로운 사용자를 등록합니다."
    )
    @PostMapping("/new")
    public ResponseEntity<ApiResponse<Long>> create(@RequestBody @Valid CreateUserRequest req) {
        Long id = userService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(id, HttpStatus.CREATED));
    }

    @Operation(
            summary = "사용자 수정 API",
            description = "사용자 ID로 사용자를 수정합니다."
    )
    @PutMapping
    public ResponseEntity<ApiResponse<Long>> update(@RequestBody @Valid UpdateUserRequest req) {
        Long id = userService.update(req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(id, HttpStatus.OK));
    }

    @Operation(
            summary = "사용자 전체 조회 API",
            description = "사용자를 전체 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAllUsers(), HttpStatus.OK));
    }

    @Operation(
            summary = "사용자 ID 조회 API",
            description = "사용자를 ID로 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getOne(@RequestParam("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id), HttpStatus.OK));
    }

    @Operation(
            summary = "사용자 삭제 API",
            description = "사용자 ID로 삭제합니다."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
