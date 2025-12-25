package com.jy.shoppy.domain.user.service;

import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.UserGrade;
import com.jy.shoppy.domain.user.repository.UserGradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserGradeService {
    private final UserGradeRepository userGradeRepository;

    /**
     * 사용자 등급 자동 업그레이드 체크
     */
    @Transactional
    public void checkAndUpgradeGrade(User user) {
        if (user == null) {
            return;
        }

        // 현재 총 구매금액
        BigDecimal totalAmount = user.getTotalPurchaseAmount();

        // 현재 등급보다 높은 등급 중 조건을 만족하는 최고 등급 조회
        UserGrade newGrade = userGradeRepository
                .findHighestEligibleGrade(totalAmount)
                .orElse(user.getUserGrade());

        // 등급이 변경되었다면 업그레이드 및 로그
        if (!newGrade.getId().equals(user.getUserGrade().getId())) {
            log.info("사용자 등급 업그레이드: userId={}, oldGrade={}, newGrade={}, totalAmount={}",
                    user.getId(),
                    user.getUserGrade().getName(),
                    newGrade.getName(),
                    totalAmount);

            user.upgradeGrade(newGrade);
        }
    }
}
