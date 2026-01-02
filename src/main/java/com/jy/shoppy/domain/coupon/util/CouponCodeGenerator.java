package com.jy.shoppy.domain.coupon.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CouponCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 고유한 쿠폰 코드 생성
     * 형식: PREFIX-YYYYMMDD-RANDOM
     * 예: WELCOME-20240315-A7K9X2
     */
    public static String generate(String prefix) {
        String datePart = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = generateRandomString(6);

        return String.format("%s-%s-%s", prefix, datePart, randomPart);
    }

    /**
     * 랜덤 문자열 생성
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
