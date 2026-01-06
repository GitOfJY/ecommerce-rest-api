package com.jy.shoppy.domain.cart.mapper;

import com.jy.shoppy.domain.cart.dto.CartProductResponse;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.jy.shoppy.domain.cart.entity.CartProduct;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CartMapper {

    /**
     * CartProduct → CartProductResponse 변환
     */
    public CartProductResponse toResponse(CartProduct cartProduct) {
        if (cartProduct == null) {
            return null;
        }

        // 1. 기본 가격
        BigDecimal price = cartProduct.getProduct().getPrice();

        // 2. 선택한 옵션 찾기
        String selectedColor = cartProduct.getSelectedColor();
        String selectedSize = cartProduct.getSelectedSize();

        // 3. 옵션이 있으면 해당 옵션의 추가 금액 더하기
        if (selectedColor != null || selectedSize != null) {
            ProductOption matchedOption = findMatchingOption(
                    cartProduct.getProduct().getOptions(),
                    selectedColor,
                    selectedSize
            );

            if (matchedOption != null) {
                price = price.add(matchedOption.getAdditionalPrice());
            } else {
                log.warn("옵션을 찾을 수 없습니다 - 상품ID: {}, 색상: {}, 사이즈: {}",
                        cartProduct.getProduct().getId(), selectedColor, selectedSize);
            }
        }

        // 4. 총 가격 = 개당 가격 × 수량
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(cartProduct.getQuantity()));

        return CartProductResponse.builder()
                .id(cartProduct.getId())
                .productId(cartProduct.getProduct().getId())
                .productName(cartProduct.getProduct().getName())
                .imageUrl(cartProduct.getProduct().getThumbnailUrl())
                .color(selectedColor)
                .size(selectedSize)
                .price(price)
                .quantity(cartProduct.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }

    /**
     * 옵션 리스트에서 색상/사이즈가 일치하는 옵션 찾기
     */
    private ProductOption findMatchingOption(
            List<ProductOption> options,
            String color,
            String size
    ) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        return options.stream()
                .filter(option -> isMatchingOption(option, color, size))
                .findFirst()
                .orElse(null);
    }

    /**
     * 옵션이 색상/사이즈와 일치하는지 확인
     */
    private boolean isMatchingOption(ProductOption option, String color, String size) {
        boolean colorMatch = (color == null && option.getColor() == null)
                || (color != null && color.equals(option.getColor()));

        boolean sizeMatch = (size == null && option.getSize() == null)
                || (size != null && size.equals(option.getSize()));

        return colorMatch && sizeMatch;
    }

    /**
     * CartProduct 리스트 → CartProductResponse 리스트 변환
     */
    public List<CartProductResponse> toResponseList(List<CartProduct> cartProducts) {
        if (cartProducts == null) {
            return List.of();
        }

        return cartProducts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cart → CartProductResponse 리스트 변환
     */
    public List<CartProductResponse> toResponses(Cart cart) {
        if (cart == null || cart.getCartProducts() == null) {
            return List.of();
        }

        return toResponseList(cart.getCartProducts());
    }
}
