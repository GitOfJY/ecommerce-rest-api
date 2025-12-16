package com.jy.shoppy.domain.cart.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.cart.dto.CartProductRequest;
import com.jy.shoppy.domain.cart.dto.CartProductResponse;
import com.jy.shoppy.domain.cart.dto.UpdateCartQuantityRequest;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.jy.shoppy.domain.cart.entity.CartProduct;
import com.jy.shoppy.domain.cart.mapper.CartMapper;
import com.jy.shoppy.domain.cart.repository.CartProductRepository;
import com.jy.shoppy.domain.cart.repository.CartQueryRepository;
import com.jy.shoppy.domain.cart.repository.CartRepository;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.repository.ProductRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductRepository productRepository;
    private final CartQueryRepository cartQueryRepository;
    private final CartMapper cartMapper;

    private static final String GUEST_CART_KEY = "GUEST_CART";

    //**장바구니 상품 수량 수정 -** 로그인된 사용자의 장바구니 특정 상품 수량 수정.
    //**장바구니 상품 삭제 -** 로그인된 사용자의 장바구니 특정 상품 삭제.

    // 장바구니 상품 추가
    public void addToCart(Long userId, CartProductRequest request, HttpSession session) {
        if (userId == null) {
            // 비회원 장바구니 - 레디스 세션
            addToCartForGuest(request, session);
        } else {
            // 회원 장바구니 - DB
            addToCartForMember(userId, request);
        }
    }

    private void addToCartForMember(Long userId, CartProductRequest request) {
        // 1. userId 맵핑된 cart 테이블 데이터 있는지 확인, 없다면 새로 생성
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> createCart(userId));

        // 2. 장바구니에 이미 존재하는 상품인지 확인
        Optional<CartProduct> existingProduct = cartProductRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        if (existingProduct.isPresent()) {
            // 2.1. 존재하는 상품이라면 수량 증가
            existingProduct.get().addQuantity(request.getQuantity());
        } else {
            // 2.2. 존재하지 않는 상품이라면 새로 추가
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));
            // TODO null > product_option으로 수정
            CartProduct cartProduct = CartProduct.createCartProduct(cart, product, null, request.getQuantity());
            cartProductRepository.save(cartProduct);
        }
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        return cartRepository.save(Cart.of(user));
    }

    private void addToCartForGuest(CartProductRequest request, HttpSession session) {
        List<CartProductResponse> guestCart = getGuestCart(session);

        Optional<CartProductResponse> existingProduct = guestCart.stream()
                .filter(product -> product.getProductId().equals(request.getProductId())).findFirst();

        if (existingProduct.isPresent()) {
            existingProduct.get().addQuantity(request.getQuantity());
        } else {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

            guestCart.add(CartProductResponse.builder()
                            .id(System.currentTimeMillis())  // 임시 ID (타임스탬프)
                            .productId(request.getProductId())
                            .productName(product.getName())
                            .price(product.getPrice())
                            .quantity(request.getQuantity())
                    .build());
        }

        session.setAttribute(GUEST_CART_KEY, guestCart);
    }

    // 장바구니 목록 조회
    @Transactional(readOnly = true)
    public List<CartProductResponse> findAllByUserId(Long userId, HttpSession session) {
        if (userId != null) {
            Cart cart = cartQueryRepository.findCartWithProducts(userId)
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_CART));
            return cartMapper.toResponses(cart);
        } else {
            return getGuestCart(session);
        }
    }

    private List<CartProductResponse> getGuestCart(HttpSession session) {
        List<CartProductResponse> cart = (List<CartProductResponse>) session.getAttribute(GUEST_CART_KEY);
        return cart != null ? cart : new ArrayList<>();
    }

    public CartProductResponse updateQuantity(Account account, Long cartProductId, UpdateCartQuantityRequest request, HttpSession session) {
        Long userId = account.getAccountId();
        if (userId == null) {
            return updateQuantityForGuest(request, cartProductId, session);
        }
        return updateQuantityForMember(userId, cartProductId, request);
    }

    private CartProductResponse updateQuantityForMember(Long userId, Long cartProductId, UpdateCartQuantityRequest request) {
        CartProduct cartProduct = cartProductRepository.findById(cartProductId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        if (!cartProduct.getCart().getUser().getId().equals(userId)) {
            throw  new ServiceException(ServiceExceptionCode.FORBIDDEN_CART_ACCESS);
        }

        cartProduct.updateQuantity(request.getQuantity());
        return cartMapper.toResponse(cartProduct);
    }

    private CartProductResponse updateQuantityForGuest(UpdateCartQuantityRequest request, Long cartProductId, HttpSession session) {
        List<CartProductResponse> guestCart = getGuestCart(session);

        CartProductResponse cartProduct = guestCart.stream()
                .filter(p -> p.getId().equals(cartProductId))
                .findFirst()
                .orElseThrow();

        cartProduct.updateQuantity(request.getQuantity());
        session.setAttribute(GUEST_CART_KEY, guestCart);

        return cartProduct;
    }



    // 장바구니 특정 물품 삭제 - productId
}
