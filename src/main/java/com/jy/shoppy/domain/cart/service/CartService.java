package com.jy.shoppy.domain.cart.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.cart.dto.*;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.jy.shoppy.domain.cart.entity.CartProduct;
import com.jy.shoppy.domain.cart.mapper.CartMapper;
import com.jy.shoppy.domain.cart.repository.CartProductRepository;
import com.jy.shoppy.domain.cart.repository.CartQueryRepository;
import com.jy.shoppy.domain.cart.repository.CartRepository;
import com.jy.shoppy.domain.prodcut.entity.Product;
import com.jy.shoppy.domain.prodcut.entity.ProductOption;
import com.jy.shoppy.domain.prodcut.repository.ProductOptionRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ProductOptionRepository productOptionRepository;
    private final CartMapper cartMapper;

    private static final String GUEST_CART_KEY = "GUEST_CART";

    // 장바구니 상품 추가
    public void addToCart(Account account, CartProductRequest request, HttpSession session) {
        Long userId = (account != null) ? account.getAccountId() : null;
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

        // 2. 옵션 검증 + 재고 확인
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));
        if (product.isHasOptions()) {
            if (request.getColor() == null || request.getSize() == null) {
                throw new ServiceException(ServiceExceptionCode.PRODUCT_OPTION_REQUIRED);
            }
            validateAndCheckStock(product.getId(), request.getColor(), request.getSize(), request.getQuantity());
        } else {
            if (product.getStock() < request.getQuantity()) {
                throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
            }
        }

        // 3. 같은 상품 + 같은 옵션 확인
        Optional<CartProduct> existing = cartProductRepository
                .findByCartIdAndProductIdAndOptions(
                        cart.getId(),
                        product.getId(),
                        request.getColor(),
                        request.getSize()
                );

        if (existing.isPresent()) {
            // 3.1. 존재하는 상품이라면 수량 증가
            CartProduct existingProduct = existing.get();
            existingProduct.addQuantity(request.getQuantity());
            cartProductRepository.save(existingProduct);
        } else {
            // 3.2. 존재하지 않는 상품이라면 새로 추가
            CartProduct cartProduct = CartProduct.createCartProduct(cart, product, request.getColor(), request.getSize(), request.getQuantity());
            cartProductRepository.save(cartProduct);
        }
    }

    // 옵션 검증 + 재고 확인
    private void validateAndCheckStock(Long productId, String color, String size, int quantity) {
        ProductOption option = productOptionRepository
                .findByProductIdAndColorAndSize(productId, color, size)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.INVALID_PRODUCT_OPTION));

        if (option.getStock() < quantity) {
            throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
        }
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        return cartRepository.save(Cart.of(user));
    }

    private void addToCartForGuest(CartProductRequest request, HttpSession session) {
        List<CartProductResponse> guestCart = getGuestCart(session);

        // 같은 상품 + 같은 옵션 찾기
        Optional<CartProductResponse> existingProduct = guestCart.stream()
                .filter(p -> p.getProductId().equals(request.getProductId()) &&
                        isSameOption(p, request.getColor(), request.getSize()))
                .findFirst();

        if (existingProduct.isPresent()) {
            existingProduct.get().addQuantity(request.getQuantity());
        } else {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

            guestCart.add(CartProductResponse.builder()
                            .id(System.currentTimeMillis())  // 임시 ID (타임스탬프)
                            .productId(request.getProductId())
                            .productName(product.getName())
                            .color(request.getColor())
                            .size(request.getSize())
                            .price(product.getPrice())
                            .quantity(request.getQuantity())
                            .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                    .build());
        }

        session.setAttribute(GUEST_CART_KEY, guestCart);
    }

    private boolean isSameOption(CartProductResponse cartProduct, String color, String size) {
        if (color == null && size == null) {
            return cartProduct.getColor() == null && cartProduct.getSize() == null;
        }
        return Objects.equals(cartProduct.getColor(), color) && Objects.equals(cartProduct.getSize(), size);
    }

    // 장바구니 목록 조회
    @Transactional(readOnly = true)
    public List<CartProductResponse> findAllByUserId(Account account, HttpSession session) {
        Long userId = (account != null) ? account.getAccountId() : null;
        if (userId != null) {
            Optional<Cart> cart = cartQueryRepository.findCartWithProducts(userId);
            return cart.map(cartMapper::toResponses)
                    .orElse(new ArrayList<>());  // 빈 카트면 빈 리스트
        } else {
            return getGuestCart(session);
        }
    }

    private List<CartProductResponse> getGuestCart(HttpSession session) {
        List<CartProductResponse> cart = (List<CartProductResponse>) session.getAttribute(GUEST_CART_KEY);
        return cart != null ? cart : new ArrayList<>();
    }

    public CartProductResponse updateOption(Account account, Long cartProductId, UpdateCartOptionRequest request, HttpSession session) {
        Long userId = (account != null) ? account.getAccountId() : null;
        if (userId == null) {
            // 비회원
            return updateOptionForGuest(cartProductId, request, session);
        } else {
            // 회원
            return updateOptionForMember(userId, cartProductId, request);
        }
    }

    private CartProductResponse updateOptionForMember(Long userId, Long cartProductId, UpdateCartOptionRequest request) {
        CartProduct cartProduct = cartProductRepository.findById(cartProductId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        if (!cartProduct.getCart().getUser().getId().equals(userId)) {
            throw  new ServiceException(ServiceExceptionCode.FORBIDDEN_CART_ACCESS);
        }

        // 옵션이 변경되었거나 수량이 증가한 경우만 재고 확인
        boolean optionChanged = !Objects.equals(cartProduct.getSelectedColor(), request.getColor())
                || !Objects.equals(cartProduct.getSelectedSize(), request.getSize());
        boolean quantityIncreased = request.getQuantity() > cartProduct.getQuantity();

        if (optionChanged || quantityIncreased) {
            Long productId = cartProduct.getProduct().getId();
            validateAndCheckStock(productId, request.getColor(), request.getSize(), request.getQuantity());
        }

        cartProduct.updateOptions(request.getColor(), request.getSize(), request.getQuantity());
        return cartMapper.toResponse(cartProduct);
    }

    private CartProductResponse updateOptionForGuest(Long cartProductId, UpdateCartOptionRequest request, HttpSession session) {
        List<CartProductResponse> guestCart = getGuestCart(session);

        CartProductResponse cartProduct = guestCart.stream()
                .filter(p -> p.getId().equals(cartProductId))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CART_ITEM_NOT_FOUND));

        Product product = productRepository.findById(cartProduct.getProductId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        if (product.isHasOptions()) {
            boolean optionChanged = !Objects.equals(cartProduct.getColor(), request.getColor()) ||
                    !Objects.equals(cartProduct.getSize(), request.getSize());
            boolean quantityIncreased = request.getQuantity() > cartProduct.getQuantity();

            if (optionChanged || quantityIncreased) {
                validateAndCheckStock(product.getId(), request.getColor(), request.getSize(), request.getQuantity());
            }
        } else {
            if (product.getStock() < request.getQuantity()) {
                throw new ServiceException(ServiceExceptionCode.INSUFFICIENT_STOCK);
            }
        }

        cartProduct.updateOptions(request.getColor(), request.getSize(), request.getQuantity());
        session.setAttribute(GUEST_CART_KEY, guestCart);
        return cartProduct;
    }

    public void deleteCartByIds(Account account, DeleteCartProductRequest productIds, HttpSession session) {
        Long userId = (account != null) ? account.getAccountId() : null;
        if  (userId == null) {
            // 비회원 삭제
            deleteCartForGuest(productIds, session);
        } else {
            // 회원 삭제
            deleteCartForMember(userId, productIds);
        }
    }

    private void deleteCartForMember(Long userId, DeleteCartProductRequest request) {
        int deletedCount = cartProductRepository.deleteByIdsAndUserId(
                request.getCartProductIds(),
                userId
        );

        if (deletedCount == 0) {
            throw new ServiceException(ServiceExceptionCode.CART_ITEM_NOT_FOUND);
        }
    }

    private void deleteCartForGuest(DeleteCartProductRequest request, HttpSession session) {
        List<CartProductResponse> guestCart = getGuestCart(session);
        int beforeSize = guestCart.size();

        guestCart.removeIf(product -> request.getCartProductIds().contains(product.getId()));

        int deletedCount = beforeSize - guestCart.size();
        if (deletedCount == 0) {
            throw new ServiceException(ServiceExceptionCode.CART_ITEM_NOT_FOUND);
        }

        session.setAttribute(GUEST_CART_KEY, guestCart);
    }

    @Transactional
    public void clearCart(Account account, HttpSession session) {
        Long userId = (account != null) ? account.getAccountId() : null;

        if (userId == null) {
            // 비회원: 세션 비우기
            session.removeAttribute(GUEST_CART_KEY);
        } else {
            // 회원: DB에서 전체 삭제
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_CART));
            cartProductRepository.deleteByCartId(cart.getId());
        }
    }
}
