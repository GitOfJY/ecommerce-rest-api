package com.jy.shoppy.domain.coupon.entity.type;

public enum IssueType {
    AUTO("자동 발급"),      // 회원가입 시 자동
    MANUAL("수동 발급"),    // 관리자가 수동
    EVENT("이벤트 발급");   // 특정 이벤트 참여 시

    private final String description;

    IssueType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
