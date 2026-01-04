package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.prodcut.dto.*;
import com.jy.shoppy.domain.category.entity.CategoryProduct;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.prodcut.entity.ProductImage;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.mapper.ProductMapper;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductOptionRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductQueryRepository;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductRedisService productRedisService;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductQueryRepository productQueryRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        // 1. 상품 등록
        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .build();
        Product savedProduct = productRepository.save(product);

        // 2. 옵션 등록
        if (req.getOptions() != null && !req.getOptions().isEmpty()) {
            for (CreateProductRequest.ProductOptionRequest optionReq : req.getOptions()) {
                ProductOption option = ProductOption.createOption(
                        savedProduct,
                        optionReq.getColor(),
                        optionReq.getSize(),
                        optionReq.getStock(),
                        optionReq.getAdditionalPrice()
                );
                savedProduct.getOptions().add(option);
            }
        }

        // 3. 카테고리 등록
        if (req.getCategoryIds() != null && !req.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
            if (categories.size() != req.getCategoryIds().size()) {
                throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_CATEGORY);
            }

            for (Category category : categories) {
                CategoryProduct cp = CategoryProduct.builder()
                        .category(category)
                        .product(savedProduct)
                        .build();
                savedProduct.getCategoryProducts().add(cp);
            }
        }

        // 4. 이미지 등록
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                boolean isThumbnail = (i == 0); // 첫 번째 이미지를 대표 이미지로
                ProductImage image = ProductImage.create(
                        savedProduct,
                        req.getImageUrls().get(i),
                        i,
                        isThumbnail
                );
                savedProduct.addImage(image);
            }
        }

        // Redis에 저장
        productRedisService.saveProduct(savedProduct);

        return productMapper.toResponse(savedProduct);
    }

    // 단건조회
    public ProductResponse getOne(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));
        return productMapper.toResponse(product);
    }

    // 다건 조회 (다중 정렬 조건) - DB 쿼리 기반
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(SortProductCond cond, Pageable pageable) {
        Page<Product> products = productQueryRepository.sortProducts(cond, pageable);
        return products.map(productMapper::toResponse);
    }

    // 조건 조회 - DB 쿼리 기반
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductsPage(SearchProductCond cond, Pageable pageable) {
        Page<Product> page = productQueryRepository.searchProductsPage(cond, pageable);
        return page.map(productMapper::toResponse);
    }

    /**
     * Redis 기반 상품 정렬 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllWithRedis(RedisSortProductCond cond, Pageable pageable) {
        long offset = pageable.getOffset();
        long limit = pageable.getPageSize();

        List<Long> productIds;

        // 가격 범위 필터링
        if (cond.minPrice() != null && cond.maxPrice() != null) {
            productIds = productRedisService.getProductIdsByPriceRange(
                    cond.minPrice(),
                    cond.maxPrice(),
                    cond.ascending(),
                    offset,
                    limit
            );
        } else {
            // 일반 정렬
            productIds = productRedisService.getProductIds(
                    cond.sortBy(),
                    cond.ascending(),
                    offset,
                    limit
            );
        }

        // Redis에서 조회한 ID 순서대로 DB에서 상품 조회
        List<Product> products = findProductsInOrder(productIds);

        // 전체 개수 조회
        Long total = productRedisService.getTotalCount(cond.sortBy());

        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, total);
    }

    /**
     * ID 순서를 유지하며 상품 조회
     */
    private List<Product> findProductsInOrder(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findAllById(productIds);

        // ID 순서 유지를 위한 맵 생성
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (p1, p2) -> p1, LinkedHashMap::new));

        // Redis에서 조회한 순서대로 정렬
        return productIds.stream()
                .map(productMap::get)
                .filter(product -> product != null)
                .toList();
    }

    // 수정
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        // 기본 정보만 update
        product.updateProduct(req);

        // 카테고리 수정
        if (req.getCategoryIds() != null) {
            updateCategories(product, req.getCategoryIds());
        }

        // 옵션 수정
        if (req.getOptions() != null) {
            updateOptions(product, req.getOptions());
        }

        // 이미지 수정
        if (req.getImageUrls() != null) {
            updateImages(product, req.getImageUrls());
        }

        // Redis 업데이트
        productRedisService.deleteProduct(id);
        productRedisService.saveProduct(product);

        return productMapper.toResponse(product);
    }

    private void updateImages(Product product, List<String> imageUrls) {
        // 빈 리스트면 전체 제거
        if (imageUrls.isEmpty()) {
            product.clearImages();
            return;
        }

        // 기존 제거 후 새로 추가
        product.clearImages();
        for (int i = 0; i < imageUrls.size(); i++) {
            boolean isThumbnail = (i == 0);
            ProductImage image = ProductImage.create(
                    product,
                    imageUrls.get(i),
                    i,
                    isThumbnail
            );
            product.addImage(image);
        }
    }

    private void updateOptions(Product product, List<UpdateProductOptionRequest> optionRequests) {
        // 빈 리스트면 전체 제거
        if (optionRequests.isEmpty()) {
            product.clearOptions();
            return;
        }

        // 기존 제거 후 새로 추가
        product.clearOptions();
        for (UpdateProductOptionRequest req : optionRequests) {
            ProductOption option = ProductOption.createOption(
                    product,
                    req.getColor(),
                    req.getSize(),
                    req.getStock(),
                    req.getAdditionalPrice()
            );
            product.addOption(option);
        }
    }

    private void updateCategories(Product product, List<Long> categoryIds) {
        // 빈 리스트면 전체 제거
        if (categoryIds.isEmpty()) {
            product.clearCategories();
            return;
        }

        // 카테고리 조회 및 검증
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_CATEGORY);
        }

        // 기존 제거 후 새로 추가
        product.clearCategories();
        for (Category category : categories) {
            CategoryProduct cp = CategoryProduct.builder()
                    .category(category)
                    .product(product)
                    .build();
            product.addCategory(cp);
        }
    }

    // 삭제
    @Transactional
    public Long deleteProduct(Long productId) {
        boolean hasCompleted = orderRepository.existsByOrderProductsProductIdAndStatus(productId, OrderStatus.COMPLETED);
        if (hasCompleted) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_ORDER_COMPLETED);
        }

        // Redis에서 삭제
        productRedisService.deleteProduct(productId);

        productRepository.deleteById(productId);
        return productId;
    }

    /**
     * 기존 상품을 Redis에 일괄 적재 (초기화용)
     */
    @Transactional(readOnly = true)
    public void loadProductsToRedis() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            productRedisService.saveProduct(product);
        }

        log.info("Loaded {} products to Redis", products.size());
    }
}
