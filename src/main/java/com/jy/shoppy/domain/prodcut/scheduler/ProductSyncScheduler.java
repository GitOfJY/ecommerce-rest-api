package com.jy.shoppy.domain.prodcut.scheduler;

import com.jy.shoppy.domain.prodcut.dto.ProductSyncResult;
import com.jy.shoppy.domain.prodcut.service.ProductSyncService;
import com.jy.shoppy.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 외부 상품 동기화 스케줄러
 * - 매 시간 정각에 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSyncScheduler {
    private final ProductSyncService productSyncService;

    /**
     * 매 시간 정각에 외부 상품 동기화 실행
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "${external.product.sync.cron:0 0 * * * *}")
    public void scheduledSync() {
        log.info("===== 스케줄된 외부 상품 동기화 시작 =====");

        try {
            ProductSyncResult result = productSyncService.syncExternalProducts();
            log.info("스케줄된 동기화 완료: {}", result);

        } catch (ServiceException e) {
            log.error("외부 API 오류로 동기화 실패 - code: {}, message: {}",
                    e.getCode(), e.getMessage());
            // TODO: 필요시 알림 발송 (Slack, Email 등)

        } catch (Exception e) {
            log.error("스케줄된 동기화 중 예기치 않은 오류 발생", e);
            // TODO: 필요시 알림 발송
        }
    }
}
