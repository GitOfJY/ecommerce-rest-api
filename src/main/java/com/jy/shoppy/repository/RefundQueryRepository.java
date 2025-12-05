package com.jy.shoppy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.jy.shoppy.entity.Refund;
import com.jy.shoppy.entity.type.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.jy.shoppy.entity.QRefund.refund;

@Repository
@RequiredArgsConstructor
public class RefundQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Refund> searchRefundByUserIdPage(Long userId, RefundStatus status, Pageable pageable) {
        List<Refund> contentQuery = queryFactory
                .selectFrom(refund)
                .where(
                    refundStatusEq(status),
                    userIdEq(userId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(refund.count())
                .from(refund)
                .where(
                        refundStatusEq(status),
                        userIdEq(userId)
                );
        return PageableExecutionUtils.getPage(contentQuery, pageable, countQuery::fetchOne);
    }

    private BooleanExpression refundStatusEq(RefundStatus refundStatus) {
        return refundStatus != null ? refund.status.eq(refundStatus) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? refund.user.id.eq(userId) : null;
    }
}
