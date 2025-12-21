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

import java.util.ArrayList;
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
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public Long create(CreateProductRequest req) {
        // 카테고리 확인
        List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
        if (categories.size() != req.getCategoryIds().size()) {
            throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY);
        }

        // 상품 등록
        Product product = productMapper.toEntity(req);
        Product savedProduct = productRepository.save(product);

        // 카테고리 양방향 맵핑
        List<CategoryProduct> mappings = new ArrayList<>();
        for (Category category : categories) {
            CategoryProduct cp = CategoryProduct.builder()
                    .category(category)
                    .product(product)
                    .build();
            mappings.add(cp);
        }
        product.getCategoryProducts().addAll(mappings);

        // 옵션 생성
        req.getOptions().forEach((optionReq) -> {
            ProductOption option = ProductOption.createOption(
                    savedProduct,
                    optionReq.getColor(),
                    optionReq.getSize(),
                    optionReq.getStock(),
                    optionReq.getAdditionalPrice()
            );
            productOptionRepository.save(option);
        });

        return product.getId();
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
        product.updateProduct(req);

        // 카테고리 수정 있을 때만
        if (req.getCategoryIds() != null) {
            List<Long> categoryIds = req.getCategoryIds();

            if (categoryIds.isEmpty()) {
                product.getCategoryProducts().clear();
            } else {
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                if (categories.size() != categoryIds.size()) {
                    throw new ServiceException(ServiceExceptionCode.NOT_FOUND_CATEGORY);
                }

                // 기존 관계 제거
                product.getCategoryProducts().clear();

                // 양방향 관계 설정
                for (Category c : categories) {
                    CategoryProduct cp = CategoryProduct.builder()
                            .category(c)
                            .product(product)
                            .build();
                    product.getCategoryProducts().add(cp);
                }
            }
        }

        return productMapper.toResponse(product);
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
