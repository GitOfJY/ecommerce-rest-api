package com.jy.shoppy.domain.prodcut.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.type.StockStatus;
import com.jy.shoppy.domain.prodcut.dto.SearchProductCond;
import com.jy.shoppy.domain.prodcut.dto.SortProductCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class ProductQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Product> searchProductsPage(SearchProductCond cond, Pageable pageable){
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

    private BooleanExpression productKeywordContains(String productKeyword){
        return productKeyword != null ? product.name.containsIgnoreCase(productKeyword) : null;
    }

    public Page<Product> sortProducts(SortProductCond cond, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(cond))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?>[] getOrders(Pageable pageable) {
        return pageable.getSort().stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                    return switch (order.getProperty()) {
                        case "price" -> new OrderSpecifier<>(direction, product.price);
                        case "createdAt" -> new OrderSpecifier<>(direction, product.createdAt);
                        case "name" -> new OrderSpecifier<>(direction, product.name);
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
                .filter(o -> o != null) // null 제거
                .toArray(com.querydsl.core.types.OrderSpecifier[]::new);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(SortProductCond cond) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // 가격 정렬
        if (cond.getPriceSort() != null) {
            orders.add(cond.getPriceSort() == Sort.Direction.ASC
                    ? product.price.asc()
                    : product.price.desc());
        }

        // 등록일 정렬
        if (cond.getCreatedAtSort() != null) {
            orders.add(cond.getCreatedAtSort() == Sort.Direction.ASC
                    ? product.createdAt.asc()
                    : product.createdAt.desc());
        }

        // 기본 정렬 (아무 조건 없으면)
        if (orders.isEmpty()) {
            orders.add(product.createdAt.desc());
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}
