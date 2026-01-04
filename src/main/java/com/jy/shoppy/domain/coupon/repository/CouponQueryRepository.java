package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.Coupon;
import com.jy.shoppy.domain.coupon.entity.type.CouponSortType;
import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.jy.shoppy.domain.coupon.entity.QCoupon.coupon;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CouponQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 정렬 조건에 따른 쿠폰 조회
     */
    public List<Coupon> findAllSorted(CouponSortType sortType) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(sortType);
        List<Coupon> content = queryFactory
                .selectFrom(coupon)
                .orderBy(orderSpecifiers)
                .fetch();
        return content;
    }

    /**
     * 현재 유효한 쿠폰만 정렬 조회
     */
    public List<Coupon> findAvailableSorted(CouponSortType sortType) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(sortType);
        List<Coupon> content = queryFactory
                .selectFrom(coupon)
                .where(
                        coupon.startDate.isNull()
                                .or(coupon.startDate.loe(java.time.LocalDateTime.now())),
                        coupon.endDate.isNull()
                                .or(coupon.endDate.goe(java.time.LocalDateTime.now()))
                )
                .orderBy(orderSpecifiers)
                .fetch();
        return content;
    }

    /**
     * SortType을 OrderSpecifier 배열로 변환
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(CouponSortType sortType) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // 1. 정렬 조건이 있으면 추가
        if (sortType != null) {
            switch (sortType) {
                case LATEST -> {
                    orders.add(coupon.createdAt.desc());
                }
                case DISCOUNT_DESC -> {
                    // 할인순: FIXED 우선, PERCENT 후순위
                    orders.add(
                            new CaseBuilder()
                                    .when(coupon.discountType.eq(DiscountType.FIXED)).then(1)
                                    .when(coupon.discountType.eq(DiscountType.PERCENT)).then(2)
                                    .otherwise(3)
                                    .asc()
                    );
                    // 같은 타입 내에서 할인값 높은 순
                    orders.add(coupon.discountValue.desc());
                }
                case EXPIRY_ASC -> {
                    orders.add(coupon.endDate.asc().nullsLast());
                }
            }
        } else {
            // 2. 정렬 조건이 없으면 기본 정렬 (최신순)
            orders.add(coupon.createdAt.desc());
        }

        // 3. 보조 정렬: id로 안정적인 정렬 (같은 값일 때)
        orders.add(coupon.id.desc());

        return orders.toArray(new OrderSpecifier[0]);
    }
}