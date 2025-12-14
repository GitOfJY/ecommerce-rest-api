package com.jy.shoppy.domain.order.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.jy.shoppy.domain.order.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Order> searchOrdersPage(SearchOrderCond cond, Pageable pageable) {
        List<Order> content = queryFactory
                .selectFrom(order)
            .where(
                    userIdEq(cond.userId()),
                    orderStatusEq(cond.orderStatus()),
                    dateBetween(cond.startDate(), cond.endDate())
            )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrders(pageable))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(
                    orderStatusEq(cond.orderStatus()),
                    dateBetween(cond.startDate(), cond.endDate())
                );
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? order.user.id.eq(userId) : null;
    }

    private BooleanExpression orderStatusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }

    private BooleanExpression dateBetween(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return order.orderDate.between(start, end);
        }
        if (start != null) {
            return order.orderDate.goe(start);
        }
        if (end != null) {
            return order.orderDate.loe(end);
        }
        return null;
    }

    private OrderSpecifier<?>[] getOrders(Pageable pageable) {
        return pageable.getSort().stream()
                .map(sort -> {

                    com.querydsl.core.types.Order direction =
                            sort.isAscending()
                                    ? com.querydsl.core.types.Order.ASC
                                    : com.querydsl.core.types.Order.DESC;

                    return switch (sort.getProperty()) {
                        case "orderDate" -> new OrderSpecifier<>(direction, order.orderDate);
                        case "status"    -> new OrderSpecifier<>(direction, order.status);
                        case "id"        -> new OrderSpecifier<>(direction, order.id);
                        default          -> null;
                    };
                })
                .filter(Objects::nonNull)
                .toArray(OrderSpecifier[]::new);
    }
}
