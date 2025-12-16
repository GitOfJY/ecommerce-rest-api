package com.jy.shoppy.domain.cart.dto;

import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartProductResponse {
    private Long id;  // 회원: CartProduct.id, 비회원: 임시 UUID

    // 이름, 가격, 수량, TODO : (배송정보, 배송비, 상품옵션) 추가
    // TODO : totalPrice 추가하기
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;

    // 옵션 정보 추가
    private Long optionId;
    private String color;
    private String size;
    private BigDecimal additionalPrice;

    // 수량 증가 (장바구니 담기에서 사용)
    public void addQuantity(int quantity) {
        this.quantity += quantity;
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));
    }

    // 수량 변경 (장바구니 수정에서 사용)
    public void updateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ServiceException(ServiceExceptionCode.INVALID_QUANTITY);
        }
        this.quantity = quantity;  // = 할당 연산자로 수정!
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));
    }
}