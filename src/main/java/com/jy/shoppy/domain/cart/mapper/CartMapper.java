package com.jy.shoppy.domain.cart.mapper;

import com.jy.shoppy.domain.cart.dto.CartProductResponse;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.jy.shoppy.domain.cart.entity.CartProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "product.id",   target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price",target = "price")
    @Mapping(source = "quantity",     target = "quantity")
    @Mapping(target = "totalPrice", ignore = true)
    CartProductResponse toResponse(CartProduct cartProduct);

    List<CartProductResponse> toResponseList(List<CartProduct> cartProducts);

    default List<CartProductResponse> toResponses(Cart cart) {
        if (cart == null || cart.getCartProducts() == null) {
            return List.of();
        }
        return toResponseList(cart.getCartProducts());
    }
}
