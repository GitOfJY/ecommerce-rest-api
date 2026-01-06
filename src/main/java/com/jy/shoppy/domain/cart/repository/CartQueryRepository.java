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
import static com.jy.shoppy.domain.prodcut.entity.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class CartQueryRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 장바구니 + 장바구니 상품 + 상품 정보 조회 (N+1 방지)
     * Product.options는 @BatchSize로 N+1 방지
     */
    public Optional<Cart> findCartWithProducts(Long userId) {
        Cart result = queryFactory
                .selectFrom(cart)
                .leftJoin(cart.cartProducts, cartProduct).fetchJoin()
                .leftJoin(cartProduct.product, product).fetchJoin()
                // product.options fetch join 제거 (BatchSize로 자동 처리)
                .where(cart.user.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
