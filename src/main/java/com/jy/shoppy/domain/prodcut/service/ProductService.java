package com.jy.shoppy.domain.prodcut.service;

import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.prodcut.dto.*;
import com.jy.shoppy.domain.category.entity.CategoryProduct;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductQueryRepository productQueryRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        // 상품 등록
        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .hasOptions(!req.getOptions().isEmpty())
                .build();
        Product savedProduct = productRepository.save(product);

        // 옵션 등록
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

        // 카테고리 등록
        if (req.getCategoryIds() != null && !req.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
            if (categories.size() != req.getCategoryIds().size()) {
                throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY);
            }

            for (Category category : categories) {
                CategoryProduct cp = CategoryProduct.builder()
                        .category(category)
                        .product(savedProduct)
                        .build();
                savedProduct.getCategoryProducts().add(cp);
            }
        }

        return productMapper.toResponse(savedProduct);
    }

    // 단건조회
    public ProductResponse getOne(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return productMapper.toResponse(product);
    }

    // 다건 조회 (다중 정렬 조건)
    public Page<ProductResponse> getAll(SortProductCond cond, Pageable pageable) {
        Page<Product> page = productQueryRepository.sortProducts(cond, pageable);
        return page.map(productMapper::toResponse);
    }

    // 조건 조회
    public Page<ProductResponse> searchProductsPage(SearchProductCond cond, Pageable pageable) {
        Page<Product> page = productQueryRepository.searchProductsPage(cond, pageable);
        return page.map(productMapper::toResponse);
    }

    // 수정
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        // 기본 정보만 update
        product.updateProduct(req);

        // 옵션 수정
        if (req.getOptions() != null) {
            updateOptions(product, req.getOptions());
        }

        // 카테고리 수정
        if (req.getCategoryIds() != null) {
            updateCategories(product, req.getCategoryIds());
        }

        return productMapper.toResponse(product);
    }

    private void updateOptions(Product product, List<UpdateProductOptionRequest> optionRequests) {
        // 빈 리스트면 전체 제거
        if (optionRequests.isEmpty()) {
            product.clearOptions();
            product.setHasOptions(false);
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
        product.setHasOptions(true);
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
            throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY);
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
        productRepository.deleteById(productId);
        return productId;
    }
}
