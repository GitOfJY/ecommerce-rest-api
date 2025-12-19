package com.jy.shoppy.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ServiceExceptionCode {
    UNAUTHORIZED_ACCESS("인증되지 않은 접근입니다."),
    UNAUTHORIZED("권한이 없습니다."),
    JSON_PROCESSING_ERROR("Json 데이터 처리 중 에러가 발생하였습니다."),

    NOT_AUTHENTICATED("로그인이 필요합니다."),
    INVALID_AUTH_PRINCIPAL("인증 정보를 처리할 수 없습니다."),
    GUEST_PASSWORD_REQUIRED("비회원 주문시 guestPassword는 필수입니다."),

    DUPLICATE_USER_EMAIL("이미 가입된 이메일입니다."),
    CANNOT_FOUND_USER("존재하지 않는 회원입니다."),
    UNAUTHORIZED_PASSWORD("비밀번호가 일치하지 않습니다."),
    ALREADY_WITHDRAWN_USER("탈퇴한 회원입니다."),

    ADMIN_ONLY_REFUND_PROCESS("관리자만 환불 처리가 가능합니다."),

    CANNOT_FOUND_PRODUCT("상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK("상품의 재고가 부족합니다."),

    DUPLICATED_CATEGORY_NAME("이미 존재하는 카테고리 이름입니다."),
    NOT_FOUND_CATEGORY("카테고리가 존재하지 않습니다."),
    NOT_FOUND_PARENT_CATEGORY("상위 카테고리가 존재하지 않습니다."),
    CANNOT_DELETE_HAS_CHILD("하위 카테고리가 존재합니다."),
    CANNOT_DELETE_HAS_PRODUCTS("카테고리에 속한 상품들이 존재합니다."),

    CANNOT_CANCEL_ORDER_COMPLETED("주문 완료된 건은 취소가 불가합니다."),
    CANNOT_CANCEL_ORDER_CANCELED("이미 취소된 주문입니다."),
    CANNOT_FOUND_ORDER("주문을 찾을 수 없습니다."),
    CANNOT_DELETE_ORDER_COMPLETED("완료된 주문이 있어 삭제할 수 없습니다."),
    CANNOT_DELETE_ORDER_NOT_PENDING("PENDING 상태의 주문만 취소할 수 있습니다."),

    CANNOT_REQUEST_REFUND_NOT_COMPLETED("COMPLETED 상태의 주문만 환불 요청이 가능합니다."),
    CANNOT_FOUND_REFUND("해당하는 환불건을 찾을 수 없습니다."),

    CANNOT_FOUND_CART("해당하는 장바구니를 찾을 수 없습니다."),
    CANNOT_FOUND_CART_PRODUCT("해당하는 장바구니의 상품을 찾을 수 없습니다."),
    FORBIDDEN_CART_ACCESS("본인의 장바구니만 접근할 수 있습니다."),
    INVALID_QUANTITY("수량은 0보다 크거나 같아야 합니다."),

    CANNOT_FOUND_DELIVERY_ADDRESS("해당하는 주소를 찾을 수 없습니다."),
    // ... 다른 예외 코드들
    ;

    final String message;
}
