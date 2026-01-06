package com.jy.shoppy.domain.cart.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.cart.dto.*;
import com.jy.shoppy.domain.cart.entity.Cart;
import com.jy.shoppy.domain.cart.entity.CartProduct;
import com.jy.shoppy.domain.cart.mapper.CartMapper;
import com.jy.shoppy.domain.cart.repository.CartProductRepository;
import com.jy.shoppy.domain.cart.repository.CartQueryRepository;
import com.jy.shoppy.domain.cart.repository.CartRepository;
import com.jy.shoppy.domain.coupon.dto.MaxDiscountCouponResponse;
import com.jy.shoppy.domain.coupon.service.CouponService;
import com.jy.shoppy.domain.order.dto.OrderProductsRequest;
import com.jy.shoppy.domain.prodcut.dto.OrderProductRequest;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CartService {
    private final CouponService couponService;
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

        validateAndCheckStock(product.getId(), request.getColor(), request.getSize(), request.getQuantity());

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
        int existingIndex = findGuestCartItemIndex(guestCart, request.getProductId(), request.getColor(), request.getSize());

        if (existingIndex >= 0) {
            CartProductResponse existing = guestCart.get(existingIndex);
            CartProductResponse updated = increaseGuestCartItemQuantity(existing, request.getQuantity());
            guestCart.set(existingIndex, updated);
        } else {
            // 새 상품 추가
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

            CartProductResponse newItem = createGuestCartItem(product, request.getColor(), request.getSize(), request.getQuantity());
            guestCart.add(newItem);
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

    /**
     * 세션에서 비회원 장바구니 조회
     */
    @SuppressWarnings("unchecked")
    private List<CartProductResponse> getGuestCart(HttpSession session) {
        List<CartProductResponse> cart = (List<CartProductResponse>) session.getAttribute(GUEST_CART_KEY);
        return cart != null ? cart : new ArrayList<>();
    }

    /**
     * 비회원 장바구니 아이템 생성 (새로운 DTO 생성)
     */
    private CartProductResponse createGuestCartItem(Product product, String color, String size, int quantity) {
        // 옵션 가격 계산
        BigDecimal price = product.getPrice();
        if (color != null || size != null) {
            ProductOption option = productOptionRepository
                    .findByProductIdAndColorAndSize(product.getId(), color, size)
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.INVALID_PRODUCT_OPTION));
            price = price.add(option.getAdditionalPrice());
        }

        // 총 가격 계산
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));

        return CartProductResponse.builder()
                .id(System.currentTimeMillis())  // 임시 ID
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(product.getThumbnailUrl())
                .color(color)
                .size(size)
                .price(price)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .build();
    }

    /**
     * 비회원 장바구니 아이템 수량 증가 (새로운 DTO 생성)
     */
    private CartProductResponse increaseGuestCartItemQuantity(CartProductResponse existing, int additionalQuantity) {
        int newQuantity = existing.getQuantity() + additionalQuantity;
        BigDecimal newTotalPrice = existing.getPrice().multiply(BigDecimal.valueOf(newQuantity));

        return CartProductResponse.builder()
                .id(existing.getId())
                .productId(existing.getProductId())
                .productName(existing.getProductName())
                .imageUrl(existing.getImageUrl())
                .color(existing.getColor())
                .size(existing.getSize())
                .price(existing.getPrice())
                .quantity(newQuantity)
                .totalPrice(newTotalPrice)
                .build();
    }

    /**
     * 비회원 장바구니에서 상품 인덱스 찾기 (상품ID + 옵션)
     */
    private int findGuestCartItemIndex(
            List<CartProductResponse> cart,
            Long productId,
            String color,
            String size
    ) {
        for (int i = 0; i < cart.size(); i++) {
            CartProductResponse item = cart.get(i);
            if (item.getProductId().equals(productId) && isSameOption(item, color, size)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 비회원 장바구니에서 ID로 인덱스 찾기
     */
    private int findGuestCartItemIndexById(List<CartProductResponse> cart, Long id) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
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

        // 해당 아이템 찾기
        int index = findGuestCartItemIndexById(guestCart, cartProductId);
        if (index < 0) {
            throw new ServiceException(ServiceExceptionCode.CART_ITEM_NOT_FOUND);
        }

        CartProductResponse existing = guestCart.get(index);

        // 재고 확인
        validateAndCheckStock(
                existing.getProductId(),
                request.getColor(),
                request.getSize(),
                request.getQuantity()
        );

        // 옵션 변경 (Service에서 처리)
        Product product = productRepository.findById(existing.getProductId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        CartProductResponse updated = createGuestCartItem(
                product,
                request.getColor(),
                request.getSize(),
                request.getQuantity()
        );

        guestCart.set(index, updated);
        session.setAttribute(GUEST_CART_KEY, guestCart);

        return updated;
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

    /**
     * 장바구니 종합 정보 조회 (회원 전용)
     * - 상품 총액
     * - 회원 등급 할인
     * - 최대 쿠폰 할인
     * - 적립 예정 포인트
     * - 최종 결제 금액
     */
    @Transactional(readOnly = true)
    public CartSummaryResponse getCartSummary(Account account) {
        Long userId = account.getAccountId();

        // 1. 회원 정보 조회 (등급 정보 포함)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        // 2. 회원 장바구니 조회 (fetch join으로 N+1 방지)
        Cart cart = cartQueryRepository.findCartWithProducts(userId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_CART));

        // 3. 장바구니가 비어있는지 확인
        if (cart.getCartProducts().isEmpty()) {
            return createEmptySummary();
        }

        // 4. 상품 총액 계산 (옵션 가격 포함)
        BigDecimal totalAmount = calculateTotalAmount(cart);

        // 5. 회원 등급 할인 계산
        BigDecimal memberGradeDiscountRate = user.getUserGrade().getDiscountRate();
        BigDecimal memberGradeDiscountAmount = totalAmount
                .multiply(memberGradeDiscountRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);

        // 6. 등급 할인 적용 후 금액
        BigDecimal amountAfterGradeDiscount = totalAmount.subtract(memberGradeDiscountAmount);

        // 7. 장바구니 → OrderProductsRequest 변환
        OrderProductsRequest orderRequest = convertCartToOrderRequest(cart);

        // 8. 최대 할인 쿠폰 계산
        MaxDiscountCouponResponse maxCoupon = couponService.calculateMaxDiscount(orderRequest, account);

        // 9. 최종 결제 금액 = 등급 할인 적용 후 금액 - 쿠폰 할인
        BigDecimal finalPaymentAmount = amountAfterGradeDiscount.subtract(maxCoupon.getMaxDiscountAmount());

        // 10. 적립 예정 포인트 계산
        BigDecimal pointRate = user.getUserGrade().getPointRate();
        Integer expectedPoints = finalPaymentAmount
                .multiply(pointRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .intValue();

        // 11. 총 할인 금액
        BigDecimal totalDiscountAmount = memberGradeDiscountAmount.add(maxCoupon.getMaxDiscountAmount());

        // 12. 장바구니 상품 목록
        List<CartProductResponse> cartProducts = cartMapper.toResponses(cart);

        return CartSummaryResponse.builder()
                .totalAmount(totalAmount)
                .memberGradeDiscountRate(memberGradeDiscountRate)
                .memberGradeDiscountAmount(memberGradeDiscountAmount)
                .maxCouponDiscountAmount(maxCoupon.getMaxDiscountAmount())
                .totalDiscountAmount(totalDiscountAmount)
                .finalPaymentAmount(finalPaymentAmount)
                .expectedPoints(expectedPoints)
                .pointRate(pointRate)
                .bestCoupon(maxCoupon.getBestCoupon())
                .applicableCoupons(maxCoupon.getApplicableCoupons())
                .cartProducts(cartProducts)
                .build();
    }

    /**
     * 장바구니 상품 총액 계산 (옵션 가격 포함)
     */
    private BigDecimal calculateTotalAmount(Cart cart) {
        return cart.getCartProducts().stream()
                .map(this::calculateCartProductAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 개별 장바구니 상품 금액 계산 (옵션 가격 포함)
     */
    private BigDecimal calculateCartProductAmount(CartProduct cartProduct) {
        Product product = cartProduct.getProduct();
        BigDecimal price = product.getPrice();

        // 옵션이 있는 경우 옵션 가격 추가
        String color = cartProduct.getSelectedColor();
        String size = cartProduct.getSelectedSize();

        if (color != null || size != null) {
            ProductOption option = productOptionRepository
                    .findByProductIdAndColorAndSize(product.getId(), color, size)
                    .orElse(null);

            if (option != null) {
                price = price.add(option.getAdditionalPrice());
            }
        }

        // 가격 × 수량
        return price.multiply(BigDecimal.valueOf(cartProduct.getQuantity()));
    }

    /**
     * 빈 장바구니 응답 생성
     */
    private CartSummaryResponse createEmptySummary() {
        return CartSummaryResponse.builder()
                .totalAmount(BigDecimal.ZERO)
                .memberGradeDiscountRate(BigDecimal.ZERO)
                .memberGradeDiscountAmount(BigDecimal.ZERO)
                .maxCouponDiscountAmount(BigDecimal.ZERO)
                .totalDiscountAmount(BigDecimal.ZERO)
                .finalPaymentAmount(BigDecimal.ZERO)
                .expectedPoints(0)
                .pointRate(BigDecimal.ZERO)
                .bestCoupon(null)
                .applicableCoupons(List.of())
                .cartProducts(List.of())
                .build();
    }

    /**
     * 장바구니 → OrderProductsRequest 변환
     */
    private OrderProductsRequest convertCartToOrderRequest(Cart cart) {
        List<OrderProductRequest> orderProducts = cart.getCartProducts().stream()
                .map(this::convertCartProductToOrderProduct)
                .collect(Collectors.toList());

        return OrderProductsRequest.builder()
                .products(orderProducts)
                .build();
    }

    /**
     * CartProduct → OrderProductRequest 변환
     */
    private OrderProductRequest convertCartProductToOrderProduct(CartProduct cartProduct) {
        return OrderProductRequest.builder()
                .productId(cartProduct.getProduct().getId())
                .color(cartProduct.getSelectedColor())
                .size(cartProduct.getSelectedSize())
                .quantity(cartProduct.getQuantity())
                .build();
    }
}
