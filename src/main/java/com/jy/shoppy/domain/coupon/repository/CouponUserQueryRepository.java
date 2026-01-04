package com.jy.shoppy.domain.coupon.repository;

import com.jy.shoppy.domain.coupon.entity.CouponUser;
import com.jy.shoppy.domain.coupon.entity.type.CouponSortType;
import com.jy.shoppy.domain.coupon.entity.type.DiscountType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.jy.shoppy.domain.coupon.entity.QCoupon.coupon;
import static com.jy.shoppy.domain.coupon.entity.QCouponUser.couponUser;

@Repository
@RequiredArgsConstructor
public class CouponUserQueryRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 사용자의 쿠폰 조회 (정렬)
     */
    public List<CouponUser> findAllByUserIdSorted(Long userId, CouponSortType sortType) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(sortType);

        List<CouponUser> content = queryFactory
                .selectFrom(couponUser)
                .join(couponUser.coupon, coupon).fetchJoin()
                .where(couponUser.user.id.eq(userId))
                .orderBy(orderSpecifiers)
                .fetch();

        return content;
    }

    /**
     * 사용자의 사용 가능한 쿠폰만 조회 (정렬)
     */
    public List<CouponUser> findAvailableByUserIdSorted(Long userId, CouponSortType sortType) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(sortType);

        List<CouponUser> content = queryFactory
                .selectFrom(couponUser)
                .join(couponUser.coupon, coupon).fetchJoin()
                .where(
                        couponUser.user.id.eq(userId),
                        couponUser.status.eq(com.jy.shoppy.domain.coupon.entity.type.CouponStatus.ISSUED),
                        couponUser.expiresAt.after(java.time.LocalDateTime.now())
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
                    // 최신 등록순
                    orders.add(couponUser.issuedAt.desc());
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
                    // 만료 임박순
                    orders.add(couponUser.expiresAt.asc());
                }
            }
        } else {
            // 2. 정렬 조건이 없으면 기본 정렬 (최신 등록순)
            orders.add(couponUser.issuedAt.desc());
        }

        // 3. 보조 정렬: id로 안정적인 정렬 (같은 값일 때)
        orders.add(couponUser.id.desc());

        return orders.toArray(new OrderSpecifier[0]);
    }
}