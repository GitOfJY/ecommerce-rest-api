package com.jy.shoppy.domain.coupon.service;

import com.jy.shoppy.domain.coupon.dto.*;
import com.jy.shoppy.domain.coupon.entity.Coupon;
import com.jy.shoppy.domain.coupon.entity.CouponUser;
import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
import com.jy.shoppy.domain.coupon.mapper.CouponMapper;
import com.jy.shoppy.domain.coupon.repository.CouponRepository;
import com.jy.shoppy.domain.coupon.repository.CouponUserRepository;
import com.jy.shoppy.domain.coupon.util.CouponCodeGenerator;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;
    private final CouponMapper couponMapper;

    /**
     * 쿠폰 생성
     */
    @Transactional
    public CouponResponse create(CreateCouponRequest req) {
        // 1. 쿠폰명 중복 체크
        if (couponRepository.existsByName(req.getName())) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_COUPON_NAME);
        }

        // 2. 날짜 유효성 검증
        validateCouponDates(req);

        // 3. 쿠폰 생성 및 저장
        Coupon coupon = Coupon.create(req);
        couponRepository.save(coupon);

        return couponMapper.toCouponResponse(coupon);
    }
    /**
     * 쿠폰 대량 발급
     */
    @Transactional
    public IssueCouponResponse issueCoupons(Long couponId, IssueCouponRequest request) {
        // 1. 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        // 2. 유효한 쿠폰인지 확인
        if (!coupon.isValid()) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_PERIOD);
        }

        // 3. 발급 가능 수량 확인
        if (!coupon.canIssueMore(request.getQuantity())) {
            throw new ServiceException(ServiceExceptionCode.EXCEEDED_COUPON_LIMIT);
        }

        // 4. 만료일 계산
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(coupon.getValidDays());

        // 5. 고유한 쿠폰 코드 생성 및 저장 (트랜잭션 보장)
        List<CouponUser> couponUsers = generateCouponCodes(coupon, request.getQuantity(), expiresAt);
        couponUserRepository.saveAll(couponUsers);

        // 6. 발급 수 증가
        coupon.increaseIssueCount(request.getQuantity());

        // 7. 생성된 쿠폰 코드 목록
        List<String> couponCodes = couponUsers.stream()
                .map(CouponUser::getCode)
                .collect(Collectors.toList());
        return couponMapper.toIssueCouponResponse(coupon, request.getQuantity(), couponCodes, expiresAt);
    }

    /**
     * 쿠폰 수정
     */
    @Transactional
    public CouponResponse update(Long couponId, UpdateCouponRequest request) {
        // 1. 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        // 2. 쿠폰명 중복 체크 (본인 제외)
        couponRepository.findByName(request.getName())
                .ifPresent(existingCoupon -> {
                    if (!existingCoupon.getId().equals(couponId)) {
                        throw new ServiceException(ServiceExceptionCode.DUPLICATE_COUPON_NAME);
                    }
                });
        // 3. 날짜 유효성 검증
        validateUpdateCouponDates(request);

        // 4. 쿠폰 업데이트
        coupon.update(request);

        return couponMapper.toCouponResponse(coupon);
    }

        /**
         * 쿠폰 삭제
         */
    @Transactional
    public void delete(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        // 발급된 쿠폰이 있으면 삭제 불가
        if (coupon.getIssueCount() > 0) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_ISSUED_COUPON);
        }

        couponRepository.delete(coupon);
    }

    /**
     * 쿠폰 단건 조회
     */
    public CouponResponse findById(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));
        return couponMapper.toCouponResponse(coupon);
    }

    /**
     * 쿠폰 전체 조회
     */
    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰 생성 시 날짜 유효성 검증
     */
    private void validateCouponDates(CreateCouponRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_DATE);
            }
        }
    }

    /**
     * 쿠폰 수정 시 날짜 유효성 검증
     */
    private void validateUpdateCouponDates(UpdateCouponRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_DATE);
            }
        }
    }

    /**
     * 쿠폰 코드 대량 생성
     */
    private List<CouponUser> generateCouponCodes(Coupon coupon, int quantity, LocalDateTime expiresAt) {
        List<CouponUser> couponUsers = new ArrayList<>();
        Set<String> existingCodes = new HashSet<>();

        // 쿠폰명 앞 6자를 prefix로 사용 (한글 제외)
        String prefix = extractPrefix(coupon.getName());

        for (int i = 0; i < quantity; i++) {
            String code = generateUniqueCouponCode(prefix, existingCodes);
            existingCodes.add(code);

            CouponUser couponUser = CouponUser.builder()
                    .coupon(coupon)
                    .code(code)
                    .expiresAt(expiresAt)
                    .status(CouponStatus.AVAILABLE)
                    .build();

            couponUsers.add(couponUser);
        }
        return couponUsers;
    }

    /**
     * 고유한 쿠폰 코드 생성
     */
    private String generateUniqueCouponCode(String prefix, Set<String> existingCodes) {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = CouponCodeGenerator.generate(prefix);
            attempts++;

            if (attempts >= maxAttempts) {
                throw new ServiceException(ServiceExceptionCode.COUPON_CODE_GENERATION_FAILED);
            }
        } while (existingCodes.contains(code) ||
                couponUserRepository.existsByCode(code));
        return code;
    }

    /**
     * 쿠폰명에서 prefix 추출 (영문/숫자만, 최대 6자)
     */
    private String extractPrefix(String name) {
        if (name == null || name.isEmpty()) {
            return "COUPON";
        }

        // 영문/숫자만 추출
        String alphanumeric = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        if (alphanumeric.isEmpty()) {
            return "COUPON";
        }

        // 최대 6자까지만 사용
        return alphanumeric.length() > 6 ? alphanumeric.substring(0, 6) : alphanumeric;
    }

    /**
     * 쿠폰 등록 유효성 검증
     */
    private void validateCouponRegistration(CouponUser couponUser, Long userId) {
        // 1. status 확인: AVAILABLE이 아니면 등록 불가
        if (couponUser.getStatus() != CouponStatus.AVAILABLE) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_REGISTERED_COUPON);
        }

        // 2. user_id 확인: 이미 할당된 쿠폰인지 확인
        if (couponUser.getUser() != null) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_ASSIGNED_COUPON);
        }

        // 3. 만료 확인
        if (couponUser.isExpired()) {
            couponUser.expire();
            throw new ServiceException(ServiceExceptionCode.EXPIRED_COUPON);
        }

        // 4. 쿠폰 템플릿 유효 기간 확인
        if (!couponUser.getCoupon().isValid()) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_PERIOD);
        }

        // 5. 중복 등록 방지
        boolean alreadyHas = couponUserRepository.existsByUserIdAndCouponId(
                userId,
                couponUser.getCoupon().getId()
        );
        if (alreadyHas) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_COUPON_REGISTRATION);
        }
    }
}
