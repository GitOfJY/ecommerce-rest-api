package com.jy.shoppy.infra.external;

import com.jy.shoppy.domain.prodcut.dto.ExternalProduct;
import com.jy.shoppy.domain.prodcut.dto.external.ExternalApiResponse;
import com.jy.shoppy.domain.prodcut.dto.external.ExternalProductPage;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ExternalProductApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final int pageSize;
    private final RetryTemplate retryTemplate;

    public ExternalProductApiClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${external.product.api.base-url}") String baseUrl,
            @Value("${external.product.api.connect-timeout:5000}") int connectTimeout,
            @Value("${external.product.api.read-timeout:10000}") int readTimeout,
            @Value("${external.product.api.page-size:20}") int pageSize) {

        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .readTimeout(Duration.ofMillis(readTimeout))
                .build();
        this.baseUrl = baseUrl;
        this.pageSize = pageSize;

        // RetryTemplate 설정 (페이지별 재시도)
        this.retryTemplate = RetryTemplate.builder()
                .maxAttempts(5)
                .exponentialBackoff(1000, 2, 10000)
                .retryOn(RestClientException.class)
                .build();
    }

    /**
     * 모든 외부 상품 조회 (페이징 처리)
     */
    public List<ExternalProduct> fetchAllProducts() {
        log.info("외부 상품 API 호출 시작");
        List<ExternalProduct> allProducts = new ArrayList<>();
        int page = 0;
        boolean hasNext = true;

        while (hasNext) {
            // 페이지별로 재시도 적용
            ExternalProductPage pageResponse = fetchProductPageWithRetry(page);

            if (pageResponse == null || pageResponse.getContents() == null) {
                log.warn("외부 API 응답이 비어있음 - page: {}", page);
                break;
            }

            allProducts.addAll(pageResponse.getContents());
            hasNext = pageResponse.hasNextPage();
            page++;

            log.debug("페이지 {} 조회 완료, 현재까지 {}개 상품", page, allProducts.size());
        }

        log.info("외부 상품 API 호출 완료 - 총 {}개 상품 조회", allProducts.size());
        return allProducts;
    }

    /**
     * 페이지별 재시도 적용
     */
    private ExternalProductPage fetchProductPageWithRetry(int page) {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                log.warn("페이지 {} 재시도 중... ({}회차)", page, context.getRetryCount() + 1);
            }
            return fetchProductPage(page);
        }, context -> {
            log.error("페이지 {} 조회 실패 - 최대 재시도 횟수 초과", page);
            throw new ServiceException(ServiceExceptionCode.EXTERNAL_API_ERROR);
        });
    }

    private ExternalProductPage fetchProductPage(int page) {
        String url = String.format("%s/products?page=%d&size=%d", baseUrl, page, pageSize);

        try {
            ResponseEntity<ExternalApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    ExternalApiResponse.class
            );

            ExternalApiResponse body = response.getBody();

            if (body == null || !body.isSuccess()) {
                log.error("외부 API 응답 실패 - URL: {}", url);
                throw new RestClientException("외부 API 응답 실패");
            }

            return body.getMessage();

        } catch (RestClientException e) {
            log.error("외부 API 호출 실패 - URL: {}, 에러: {}", url, e.getMessage());
            throw e;
        }
    }

    private HttpEntity<?> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }
}