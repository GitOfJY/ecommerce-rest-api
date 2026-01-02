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
    // ========== 401 Unauthorized (인증 실패) ==========
    AUTHENTICATION_REQUIRED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS("인증되지 않은 접근입니다.", HttpStatus.UNAUTHORIZED),
    NOT_AUTHENTICATED("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_GUEST_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

    // ========== 403 Forbidden (권한 없음) ==========
    ACCESS_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("권한이 없습니다.", HttpStatus.FORBIDDEN),
    ADMIN_ONLY_REFUND_PROCESS("관리자만 환불 처리가 가능합니다.", HttpStatus.FORBIDDEN),
    FORBIDDEN_CART_ACCESS("본인의 장바구니만 접근할 수 있습니다.", HttpStatus.FORBIDDEN),

    // ========== 404 Not Found (리소스 없음) ==========
    USER_NOT_FOUND("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_USER("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_USER_GRADE("존재하지 않는 등급입니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_PRODUCT("상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_CATEGORY("카테고리가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_PARENT_CATEGORY("상위 카테고리가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_ORDER("주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_REFUND("해당하는 환불건을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_CART("해당하는 장바구니를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_CART_PRODUCT("해당하는 장바구니의 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND("장바구니에서 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_DELIVERY_ADDRESS("해당하는 주소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_REVIEW("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_FOUND_COMMENT("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // ========== 409 Conflict (중복, 충돌) ==========
    DUPLICATE_USER_EMAIL("이미 가입된 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATED_CATEGORY_NAME("이미 존재하는 카테고리 이름입니다.", HttpStatus.CONFLICT),
    REVIEW_ALREADY_EXISTS("이미 리뷰가 작성되었습니다.", HttpStatus.CONFLICT),
    ALREADY_WITHDRAWN_USER("탈퇴한 회원입니다.", HttpStatus.CONFLICT),
    ALREADY_COMPLETED_ORDER("이미 완료된 주문입니다.", HttpStatus.CONFLICT),
    CANNOT_CANCEL_ORDER_CANCELED("이미 취소된 주문입니다.", HttpStatus.CONFLICT),

    // ========== 400 Bad Request (잘못된 요청, 검증 실패) ==========
    INVALID_AUTH_PRINCIPAL("인증 정보를 처리할 수 없습니다.", HttpStatus.BAD_REQUEST),
    GUEST_PASSWORD_REQUIRED("비회원 주문시 guestPassword는 필수입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("상품의 재고가 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_OPTION("선택한 옵션이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY("수량은 1개 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    PRODUCT_OPTION_REQUIRED("옵션(색상/사이즈)을 선택해주세요.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_HAS_CHILD("하위 카테고리가 존재합니다.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_HAS_PRODUCTS("카테고리에 속한 상품들이 존재합니다.", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_ORDER_COMPLETED("주문 완료된 건은 취소가 불가합니다.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ORDER_COMPLETED("완료된 주문이 있어 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ORDER_NOT_PENDING("PENDING 상태의 주문만 취소할 수 있습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_REQUEST_REFUND_NOT_COMPLETED("COMPLETED 상태의 주문만 환불 요청이 가능합니다.", HttpStatus.BAD_REQUEST),
    DELIVERY_ADDRESS_INFO_REQUIRED("배송지 입력은 필수입니다.", HttpStatus.BAD_REQUEST),
    NOT_GUEST_ORDER("비회원 주문이 아닙니다.", HttpStatus.BAD_REQUEST),
    GUEST_ORDER_NOT_MATCH("주문자 정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_REVIEW_NOT_COMPLETED("완료된 주문만 리뷰 작성이 가능합니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARENT_COMMENT("유효하지 않은 부모 댓글입니다.", HttpStatus.BAD_REQUEST),
    EMPTY_FILE("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("파일 크기는 5MB를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // ========== 500 Internal Server Error ==========
    JSON_PROCESSING_ERROR("Json 데이터 처리 중 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED("파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    final String message;
    final HttpStatus status;
}