package com.jy.shoppy.domain.prodcut.repository;

import com.jy.shoppy.domain.prodcut.entity.type.SortType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.dto.SearchProductCond;
import com.jy.shoppy.domain.prodcut.dto.SortProductCond;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.jy.shoppy.domain.category.entity.QCategoryProduct.categoryProduct;
import static com.jy.shoppy.domain.prodcut.entity.QProduct.product;
import static com.jy.shoppy.domain.prodcut.entity.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Product> searchProductsPage(SearchProductCond cond, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .distinct()
                .leftJoin(product.categoryProducts, categoryProduct).fetchJoin()
                .leftJoin(product.options, productOption)
                .where(
                        categoryIdEq(cond.categoryId()),
                        priceGoe(cond.minPrice()),
                        priceLoe(cond.maxPrice()),
                        productKeywordContains(cond.productKeyword()),
                        stockStatusEq(cond.stockStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrders(pageable))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.countDistinct())
                .from(product)
                .leftJoin(product.categoryProducts, categoryProduct)
                .leftJoin(product.options, productOption)
                .where(
                        categoryIdEq(cond.categoryId()),
                        priceGoe(cond.minPrice()),
                        priceLoe(cond.maxPrice()),
                        productKeywordContains(cond.productKeyword()),
                        stockStatusEq(cond.stockStatus())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * SortType을 사용한 정렬
     */
    public Page<Product> sortProducts(SortProductCond cond, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(cond);
        log.info("[QueryDSL] Sorting with: {}", (Object[]) orderSpecifiers);

        List<Product> content = queryFactory
                .selectFrom(product)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers)
                .fetch();

        log.info("[QueryDSL] Fetched {} products", content.size());

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null
                ? product.categoryProducts.any().category.id.eq(categoryId)
                : null;
    }

    private BooleanExpression priceGoe(BigDecimal minPrice) {
        return minPrice != null ? product.price.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(BigDecimal maxPrice) {
        return maxPrice != null ? product.price.loe(maxPrice) : null;
    }

    private BooleanExpression stockStatusEq(StockStatus stockStatus) {
        if (stockStatus == null) {
            return null;
        }

        return switch (stockStatus) {
            case IN_STOCK -> JPAExpressions
                    .select(productOption.stock.sum().coalesce(0))
                    .from(productOption)
                    .where(productOption.product.eq(product))
                    .gt(5);
            case LOW_STOCK -> JPAExpressions
                    .select(productOption.stock.sum().coalesce(0))
                    .from(productOption)
                    .where(productOption.product.eq(product))
                    .goe(1)
                    .and(JPAExpressions
                            .select(productOption.stock.sum().coalesce(0))
                            .from(productOption)
                            .where(productOption.product.eq(product))
                            .loe(5));
            case OUT_OF_STOCK -> JPAExpressions
                    .select(productOption.stock.sum().coalesce(0))
                    .from(productOption)
                    .where(productOption.product.eq(product))
                    .eq(0);
        };
    }

    private BooleanExpression productKeywordContains(String productKeyword) {
        return productKeyword != null ? product.name.containsIgnoreCase(productKeyword) : null;
    }

    /**
     * Pageable의 Sort를 QueryDSL OrderSpecifier로 변환
     */
    private OrderSpecifier<?>[] getOrders(Pageable pageable) {
        return pageable.getSort().stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                    return switch (order.getProperty()) {
                        case "price" -> new OrderSpecifier<>(direction, product.price);
                        case "createdAt" -> new OrderSpecifier<>(direction, product.createdAt);
                        case "name" -> new OrderSpecifier<>(direction, product.name);
                        case "averageRating" -> new OrderSpecifier<>(direction, product.averageRating);
                        case "reviewCount" -> new OrderSpecifier<>(direction, product.reviewCount);
                        case "totalStock" -> new OrderSpecifier<>(
                                direction,
                                JPAExpressions
                                        .select(productOption.stock.sum().coalesce(0))
                                        .from(productOption)
                                        .where(productOption.product.eq(product))
                        );
                        default -> null;
                    };
                })
                .filter(o -> o != null)
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(SortProductCond cond) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        SortType sortType = cond.getSortType();
        // 1. 정렬 조건이 있으면 추가
        if (sortType != null) {
            OrderSpecifier<?> orderSpecifier = switch (sortType) {
                case PRICE_ASC -> product.price.asc();
                case PRICE_DESC -> product.price.desc();
                case DATE_ASC -> product.createdAt.asc();
                case DATE_DESC -> product.createdAt.desc();
                case RATING_DESC -> product.averageRating.desc();
                case REVIEW_DESC -> product.reviewCount.desc();
            };
            orders.add(orderSpecifier);
            log.info("[QueryDSL] Primary sort: {}", sortType);
        } else {
            // 2. 정렬 조건이 없으면 기본 정렬 (최신순)
            orders.add(product.createdAt.desc());
            log.info("[QueryDSL] Default sort: createdAt DESC");
        }

        // 3. 보조 정렬: id로 안정적인 정렬 (같은 값일 때)
        orders.add(product.id.desc());

        return orders.toArray(new OrderSpecifier[0]);
    }
}