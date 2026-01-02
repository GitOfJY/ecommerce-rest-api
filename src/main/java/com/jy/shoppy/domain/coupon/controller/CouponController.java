package com.jy.shoppy.domain.coupon.controller;

public class CouponController {
    // 쿠폰 대량 발급 API (POST /coupons)
    // - 요청된 발행 개수만큼 중복되지 않는 쿠폰 코드 생성하고 `coupon_user` 테이블에 초기 상태로 저장
    // - 트랜잭션 처리 : 대량 생성 시 데이터 정합성 보장

    // 쿠폰 등록 API (**`POST /coupons/Issuance`**)
    // request: couponCode, userId
    // 유효성 검증 : 쿠폰 코드의 존재 여부와 status(이미 사용되었는지) 확인
    // 상태 업데이트 : 유효한 쿠폰인 경우 user_id를 업데이트하고 status를 ISSUED로 변경
}
