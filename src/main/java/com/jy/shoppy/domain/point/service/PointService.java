package com.jy.shoppy.domain.point.service;

import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.point.entity.PointHistory;
import com.jy.shoppy.domain.point.entity.type.PointType;
import com.jy.shoppy.domain.point.repository.PointHistoryRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 적립금 적립 (주문 완료 시)
     */
    @Transactional
    public void earnPoints(User user, Order order) {
        // 1. 적립률 조회
        BigDecimal pointRate = user.getUserGrade().getPointRate();
        if (pointRate == null || pointRate.compareTo(BigDecimal.ZERO) == 0) {
            log.info("적립률이 0%입니다. 적립금 지급 생략 - 사용자ID: {}", user.getId());
            return;
        }

        // 2. 적립금 계산 (최종 결제 금액 기준)
        BigDecimal finalPrice = order.getFinalPrice();
        BigDecimal pointAmount = finalPrice.multiply(pointRate)
                .setScale(0, RoundingMode.DOWN);  // 소수점 버림

        int earnAmount = pointAmount.intValue();
        if (earnAmount <= 0) {
            log.info("적립 금액이 0원입니다. 적립 생략 - 사용자ID: {}", user.getId());
            return;
        }

        // 3. 적립금 지급
        user.addPoints(earnAmount);

        // 4. 적립 만료일 (1년 후)
        LocalDateTime expiresAt = LocalDateTime.now().plusYears(1);

        // 5. 적립 내역 저장
        PointHistory history = PointHistory.create(user, order, PointType.EARN, earnAmount, String.format("주문 완료 적립 (%s)", order.getOrderNumber()), expiresAt
        );
        pointHistoryRepository.save(history);

        log.info("적립금 지급 완료 - 사용자ID: {}, 주문ID: {}, 적립금: {}원",
                user.getId(), order.getId(), earnAmount);
    }

    /**
     * 적립금 사용 (주문 생성 시)
     */
    @Transactional
    public void usePoints(User user, Order order, int useAmount) {
        if (useAmount <= 0) {
            return;
        }

        // 1. 잔액 확인
        if (!user.hasEnoughPoints(useAmount)) {
            throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_POINTS);
        }

        // 2. 적립금 차감
        user.usePoints(useAmount);

        // 3. 사용 내역 저장
        PointHistory history = PointHistory.create(user, order, PointType.USE, -useAmount, String.format("주문 시 사용 (%s)", order.getOrderNumber()), null);
        pointHistoryRepository.save(history);

        log.info("적립금 사용 완료 - 사용자ID: {}, 주문ID: {}, 사용금액: {}원",
                user.getId(), order.getId(), useAmount);
    }

    /**
     * 적립금 복구 (주문 취소 시 - 사용한 적립금 반환)
     */
    @Transactional
    public void restoreUsedPoints(User user, Order order, int usedAmount) {
        if (usedAmount <= 0) {
            return;
        }

        // 1. 적립금 반환
        user.addPoints(usedAmount);

        // 2. 취소 내역 저장
        PointHistory history = PointHistory.create(
                user,
                order,
                PointType.CANCEL_USE,
                usedAmount,
                String.format("주문 취소로 인한 반환 (%s)", order.getOrderNumber()),
                null
        );
        pointHistoryRepository.save(history);

        log.info("적립금 반환 완료 - 사용자ID: {}, 주문ID: {}, 반환금액: {}원",
                user.getId(), order.getId(), usedAmount);
    }

    /**
     * 적립금 회수 (주문 취소 시 - 적립했던 적립금 회수)
     */
    @Transactional
    public void cancelEarnedPoints(User user, Order order) {
        // 1. 해당 주문으로 적립된 내역 조회
        PointHistory earnHistory = pointHistoryRepository
                .findByOrderIdAndPointType(order.getId(), PointType.EARN)
                .orElse(null);

        if (earnHistory == null) {
            log.info("적립 내역 없음. 회수 생략 - 주문ID: {}", order.getId());
            return;
        }

        int earnAmount = earnHistory.getAmount();

        // 2. 잔액 확인 (적립금이 부족하면 가능한 만큼만 회수)
        int cancelAmount = Math.min(user.getPoints(), earnAmount);

        // 3. 적립금 차감
        user.usePoints(cancelAmount);

        // 4. 회수 내역 저장
        PointHistory history = PointHistory.create(
                user,
                order,
                PointType.CANCEL_EARN,
                -cancelAmount,
                String.format("주문 취소로 인한 적립금 회수 (%s)", order.getOrderNumber()),
                null
        );
        pointHistoryRepository.save(history);

        log.info("적립금 회수 완료 - 사용자ID: {}, 주문ID: {}, 회수금액: {}원",
                user.getId(), order.getId(), cancelAmount);
    }
}