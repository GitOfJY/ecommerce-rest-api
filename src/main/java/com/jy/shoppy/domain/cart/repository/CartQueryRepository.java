package com.jy.shoppy.domain.cart.repository;

import com.jy.shoppy.domain.cart.dto.CartProductResponse;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.querydsl.core.QueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.jy.shoppy.domain.cart.entity.QCart.cart;
import static com.jy.shoppy.domain.cart.entity.QCartProduct.cartProduct;
import static com.jy.shoppy.domain.prodcut.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class CartQueryRepository {
    private final JPAQueryFactory queryFactory;

    // TODO : 상품 옵션/배송 상태/품절 제외 필터
    // TODO : 쿠폰 적용 여부
    // TODO : 장바구니 합계 통계
    public Optional<Cart> findCartWithProducts(Long userId) {
        Cart result = queryFactory
                .selectFrom(cart)
                .join(cart.cartProducts, cartProduct)
                .join(cartProduct.product, product)
                .where(cart.user.id.eq(userId))
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
