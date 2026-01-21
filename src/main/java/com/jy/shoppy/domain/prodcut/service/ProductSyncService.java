package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.prodcut.dto.ExternalProduct;
import com.jy.shoppy.domain.prodcut.dto.ProductSyncResult;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.mapper.ExternalProductMapper;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import com.jy.shoppy.infra.external.ExternalProductApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 외부 상품 동기화 서비스
 * - API 호출 + DB 저장을 하나의 트랜잭션으로 처리
 * - 실패 시 전체 롤백
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService {
    private final ProductRepository productRepository;
    private final ExternalProductApiClient apiClient;
    private final ExternalProductMapper externalProductMapper;

    /**
     * 외부 상품 동기화 실행
     * - 신규 상품: INSERT
     * - 기존 상품: UPDATE
     * - 외부에서 삭제된 상품: isOrderable = false
     */
    @Transactional
    public ProductSyncResult syncExternalProducts() {
        log.info("========== 외부 상품 동기화 시작 ==========");
        LocalDateTime syncStartTime = LocalDateTime.now();

        try {
            // 1. 외부 API에서 상품 목록 조회 (재시도 로직 포함)
            List<ExternalProduct> externalProducts = apiClient.fetchAllProducts();

            if (externalProducts.isEmpty()) {
                log.warn("외부 API에서 조회된 상품이 없습니다.");
                return new ProductSyncResult(0, 0, 0, 0);
            }

            log.info("외부 API에서 {}개 상품 조회됨", externalProducts.size());

            // 2. 외부 상품 ID 목록 추출
            List<String> externalIds = externalProducts.stream()
                    .map(ExternalProduct::getExternalId)
                    .toList();

            // 3. 기존 등록된 외부 상품들을 한 번에 조회 (N+1 방지)
            Map<String, Product> existingProductMap = productRepository
                    .findAllByExternalProductIdIn(externalIds)
                    .stream()
                    .collect(Collectors.toMap(Product::getExternalProductId, p -> p));

            // 4. Upsert 처리
            int insertedCount = 0;
            int updatedCount = 0;
            List<Product> productsToSave = new ArrayList<>();

            for (ExternalProduct external : externalProducts) {
                Product existingProduct = existingProductMap.get(external.getExternalId());

                if (existingProduct != null) {
                    // 기존 상품 업데이트 (Mapper 사용)
                    externalProductMapper.updateFromExternal(external, existingProduct);
                    productsToSave.add(existingProduct);
                    updatedCount++;
                    log.debug("상품 업데이트: {} - {}", external.getId(), external.getName());
                } else {
                    // 신규 상품 추가 (Mapper 사용)
                    Product newProduct = externalProductMapper.toEntity(external);
                    productsToSave.add(newProduct);
                    insertedCount++;
                    log.debug("상품 신규 등록: {} - {}", external.getId(), external.getName());
                }
            }

            // 5. Bulk 저장
            productRepository.saveAll(productsToSave);
            log.info("{}개 상품 저장 완료 (신규: {}, 업데이트: {})",
                    productsToSave.size(), insertedCount, updatedCount);

            // 6. 외부에서 삭제된 상품 비활성화 처리
            int deactivatedCount = handleDeletedProducts(syncStartTime);

            ProductSyncResult result = new ProductSyncResult(
                    insertedCount, updatedCount, deactivatedCount, externalProducts.size()
            );

            log.info("========== 외부 상품 동기화 완료: {} ==========", result);
            return result;

        } catch (ServiceException e) {
            log.error("외부 API 오류로 동기화 실패: {}", e.getMessage());
            throw e;  // 트랜잭션 롤백
        } catch (Exception e) {
            log.error("동기화 중 예기치 않은 오류 발생", e);
            throw new ServiceException(ServiceExceptionCode.PRODUCT_SYNC_FAILED);
        }
    }

    /**
     * 동기화 시 누락된 상품 = 외부에서 삭제된 상품 → 주문 불가 처리
     */
    private int handleDeletedProducts(LocalDateTime syncStartTime) {
        List<Product> staleProducts = productRepository.findStaleExternalProducts(syncStartTime);

        for (Product product : staleProducts) {
            product.markAsNotOrderable();
            log.info("동기화 누락 상품 비활성화: {} - {}",
                    product.getExternalProductId(), product.getName());
        }

        return staleProducts.size();
    }
}
