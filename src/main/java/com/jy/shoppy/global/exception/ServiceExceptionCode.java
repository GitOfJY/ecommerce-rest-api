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
    INVALID_TOKEN("잘못된 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    MISSING_TOKEN("토큰이 누락되었습니다."),
    UNAUTHORIZED_ACCESS("인증되지 않은 접근입니다."),
    JSON_PROCESSING_ERROR("Json 데이터 처리 중 에러가 발생하였습니다."),

    DUPLICATE_USER_EMAIL("이미 가입된 이메일입니다."),
    CANNOT_FOUND_USER("존재하지 않는 회원입니다."),

    ADMIN_ONLY_REFUND_PROCESS("관리자만 환불 처리가 가능합니다."),


    NOT_FOUND_PRODUCT("상품을 찾을 수 없습니다."),
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
    // ... 다른 예외 코드들
    ;

    final String message;
}
