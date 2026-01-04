package com.jy.shoppy.domain.coupon.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.category.entity.Category;
import com.jy.shoppy.domain.category.repository.CategoryRepository;
import com.jy.shoppy.domain.coupon.dto.*;
import com.jy.shoppy.domain.coupon.entity.Coupon;
import com.jy.shoppy.domain.coupon.entity.CouponCategory;
import com.jy.shoppy.domain.coupon.entity.CouponProduct;
import com.jy.shoppy.domain.coupon.entity.CouponUser;
import com.jy.shoppy.domain.coupon.entity.type.CouponApplicationType;
import com.jy.shoppy.domain.coupon.entity.type.CouponSortType;
import com.jy.shoppy.domain.coupon.entity.type.CouponStatus;
import com.jy.shoppy.domain.coupon.mapper.CouponMapper;
import com.jy.shoppy.domain.coupon.repository.*;
import com.jy.shoppy.domain.coupon.util.CouponCodeGenerator;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CouponProductRepository couponProductRepository;
    private final CouponCategoryRepository couponCategoryRepository;
    private final CouponQueryRepository couponQueryRepository;
    private final CouponUserQueryRepository couponUserQueryRepository;
    private final ProductOptionRepository productOptionRepository;
    private final CouponMapper couponMapper;

    /**
     * 쿠폰 생성
     */
    @Transactional
    public CouponResponse create(CreateCouponRequest req) {
        // 1. 쿠폰명 중복 체크
        if (couponRepository.existsByName(req.getName())) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_COUPON_NAME);
        }

        // 2. 날짜 유효성 검증
        validateCouponDates(req);

        // 3. 쿠폰 생성 및 저장
        Coupon coupon = Coupon.create(req);
        couponRepository.save(coupon);

        return couponMapper.toCouponResponse(coupon);
    }

    /**
     * 쿠폰에 적용 가능한 상품 추가
     */
    @Transactional
    public void addProducts(Long couponId, AddCouponProductsRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        if (coupon.getApplicationType() != CouponApplicationType.PRODUCT) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_APPLICATION_TYPE);
        }

        List<Product> products = productRepository.findAllById(request.getProductIds());

        if (products.size() != request.getProductIds().size()) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT);
        }

        for (Product product : products) {
            if (!couponProductRepository.existsByCouponIdAndProductId(couponId, product.getId())) {
                coupon.addProduct(product);
            }
        }
    }

    /**
     * 쿠폰에 적용 가능한 카테고리 추가
     */
    @Transactional
    public void addCategories(Long couponId, AddCouponCategoriesRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        if (coupon.getApplicationType() != CouponApplicationType.CATEGORY) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_APPLICATION_TYPE);
        }

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_CATEGORY);
        }

        for (Category category : categories) {
            if (!couponCategoryRepository.existsByCouponIdAndCategoryId(couponId, category.getId())) {
                coupon.addCategory(category);
            }
        }
    }

    /**
     * 쿠폰 적용 가능 상품 목록 조회
     */
    public List<CouponProductResponse> findCouponProducts(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        if (coupon.getApplicationType() != CouponApplicationType.PRODUCT) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_APPLICATION_TYPE);
        }

        List<CouponProduct> couponProducts = couponProductRepository.findByCouponIdWithProduct(couponId);

        return couponProducts.stream()
                .map(cp -> CouponProductResponse.builder()
                        .productId(cp.getProduct().getId())
                        .productName(cp.getProduct().getName())
                        .price(cp.getProduct().getPrice())
                        .imageUrl(cp.getProduct().getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰 적용 가능 카테고리 목록 조회
     */
    public List<CouponCategoryResponse> findCouponCategories(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        if (coupon.getApplicationType() != CouponApplicationType.CATEGORY) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_APPLICATION_TYPE);
        }

        List<CouponCategory> couponCategories = couponCategoryRepository.findByCouponIdWithCategory(couponId);

        return couponCategories.stream()
                .map(cc -> CouponCategoryResponse.builder()
                        .categoryId(cc.getCategory().getId())
                        .categoryName(cc.getCategory().getName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰 적용 가능 상품 삭제
     */
    @Transactional
    public void removeProduct(Long couponId, Long productId) {
        if (!couponProductRepository.existsByCouponIdAndProductId(couponId, productId)) {
            throw new ServiceException(ServiceExceptionCode.COUPON_PRODUCT_NOT_FOUND);
        }

        couponProductRepository.deleteByCouponIdAndProductId(couponId, productId);
    }

    /**
     * 쿠폰 적용 가능 카테고리 삭제
     */
    @Transactional
    public void removeCategory(Long couponId, Long categoryId) {
        if (!couponCategoryRepository.existsByCouponIdAndCategoryId(couponId, categoryId)) {
            throw new ServiceException(ServiceExceptionCode.COUPON_CATEGORY_NOT_FOUND);
        }

        couponCategoryRepository.deleteByCouponIdAndCategoryId(couponId, categoryId);
    }

    /**
     * 쿠폰 대량 발급
     */
    @Transactional
    public IssueCouponResponse issueCoupons(Long couponId, IssueCouponRequest request) {
        // 1. 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        // 2. 유효한 쿠폰인지 확인
        if (!coupon.isValid()) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_PERIOD);
        }

        // 3. 발급 가능 수량 확인
        if (!coupon.canIssueMore(request.getQuantity())) {
            throw new ServiceException(ServiceExceptionCode.EXCEEDED_COUPON_LIMIT);
        }

        // 4. 만료일 계산
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(coupon.getValidDays());

        // 5. 고유한 쿠폰 코드 생성 및 저장 (트랜잭션 보장)
        List<CouponUser> couponUsers = generateCouponCodes(coupon, request.getQuantity(), expiresAt);
        couponUserRepository.saveAll(couponUsers);

        // 6. 발급 수 증가
        coupon.increaseIssueCount(request.getQuantity());

        // 7. 생성된 쿠폰 코드 목록
        List<String> couponCodes = couponUsers.stream()
                .map(CouponUser::getCode)
                .collect(Collectors.toList());
        return couponMapper.toIssueCouponResponse(coupon, request.getQuantity(), couponCodes, expiresAt);
    }

    /**
     * 쿠폰 수정
     */
    @Transactional
    public CouponResponse update(Long couponId, UpdateCouponRequest request) {
        // 1. 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        // 2. 쿠폰명 중복 체크 (본인 제외)
        couponRepository.findByName(request.getName())
                .ifPresent(existingCoupon -> {
                    if (!existingCoupon.getId().equals(couponId)) {
                        throw new ServiceException(ServiceExceptionCode.DUPLICATE_COUPON_NAME);
                    }
                });

        // 3. 날짜 유효성 검증
        validateUpdateCouponDates(request);

        // 4. 쿠폰 업데이트
        coupon.update(request);

        return couponMapper.toCouponResponse(coupon);
    }

    /**
     * 쿠폰 삭제
     */
    @Transactional
    public void delete(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));

        // 발급된 쿠폰이 있으면 삭제 불가
        if (coupon.getIssueCount() > 0) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_DELETE_ISSUED_COUPON);
        }

        couponRepository.delete(coupon);
    }

    /**
     * 쿠폰 단건 조회
     */
    public CouponResponse findById(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON));
        return couponMapper.toCouponResponse(coupon);
    }

    /**
     * 쿠폰 전체 조회 (정렬)
     */
    public List<CouponResponse> findAllSorted(CouponSortType sortType) {
        List<Coupon> coupons = couponQueryRepository.findAllSorted(sortType);

        return coupons.stream()
                .map(couponMapper::toCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 모든 쿠폰 조회 (정렬)
     */
    public List<UserCouponResponse> findAllByUserId(Account account, CouponSortType sortType) {
        Long userId = account.getAccountId();
        List<CouponUser> couponUsers = couponUserQueryRepository.findAllByUserIdSorted(userId, sortType);

        return couponMapper.toUserCouponResponseList(couponUsers);
    }

    /**
     * 사용자의 사용 가능한 쿠폰만 조회 (정렬)
     */
    public List<UserCouponResponse> findAvailableByUserId(Account account, CouponSortType sortType) {
        Long userId = account.getAccountId();
        List<CouponUser> couponUsers = couponUserQueryRepository.findAvailableByUserIdSorted(userId, sortType);

        return couponMapper.toUserCouponResponseList(couponUsers);
    }

    /**
     * 쿠폰 코드로 적용 가능한 상품 조회 (등록 전)
     */
    public CouponApplicableProductsResponse findApplicableProductsByCouponCode(String couponCode) {
        CouponUser couponUser = couponUserRepository.findByCodeWithCoupon(couponCode)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.INVALID_COUPON_CODE));

        Coupon coupon = couponUser.getCoupon();

        return CouponApplicableProductsResponse.builder()
                .couponCode(couponCode)
                .couponName(coupon.getName())
                .applicationType(coupon.getApplicationType())
                .applicationTypeDescription(coupon.getApplicationType().getDescription())
                .products(getApplicableProducts(coupon))
                .categories(getApplicableCategories(coupon))
                .build();
    }

    /**
     * 내가 등록한 쿠폰의 적용 가능한 상품 조회
     */
    public CouponApplicableProductsResponse findMyApplicableProducts(Long couponUserId, Account account) {
        CouponUser couponUser = couponUserRepository.findByIdWithCoupon(couponUserId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON_USER));

        // 본인 쿠폰인지 확인
        if (!couponUser.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.FORBIDDEN_COUPON_ACCESS);
        }

        Coupon coupon = couponUser.getCoupon();
        return CouponApplicableProductsResponse.builder()
                .couponCode(couponUser.getCode())
                .couponName(coupon.getName())
                .applicationType(coupon.getApplicationType())
                .applicationTypeDescription(coupon.getApplicationType().getDescription())
                .products(getApplicableProducts(coupon))
                .categories(getApplicableCategories(coupon))
                .build();
    }

    /**
     * 특정 상품에 적용 가능한 내 쿠폰 조회 (장바구니/주문 시 사용)
     */
    public List<UserCouponResponse> findApplicableCouponsForProduct(Long productId, Account account) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT));

        Long userId = account.getAccountId();

        // 내 사용 가능한 쿠폰 조회
        List<CouponUser> myCoupons = couponUserQueryRepository.findAvailableByUserIdSorted(
                userId,
                CouponSortType.DISCOUNT_DESC
        );

        // 해당 상품에 적용 가능한 쿠폰만 필터링
        List<CouponUser> applicableCoupons = myCoupons.stream()
                .filter(cu -> isApplicableToProduct(cu.getCoupon(), product))
                .collect(Collectors.toList());

        return couponMapper.toUserCouponResponseList(applicableCoupons);
    }

    /**
     * 쿠폰의 적용 가능 상품 목록 조회
     */
    private List<CouponProductResponse> getApplicableProducts(Coupon coupon) {
        if (coupon.getApplicationType() != CouponApplicationType.PRODUCT) {
            return List.of();
        }

        List<CouponProduct> couponProducts = couponProductRepository.findByCouponIdWithProduct(coupon.getId());

        return couponProducts.stream()
                .map(cp -> CouponProductResponse.builder()
                        .productId(cp.getProduct().getId())
                        .productName(cp.getProduct().getName())
                        .price(cp.getProduct().getPrice())
                        .imageUrl(cp.getProduct().getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰의 적용 가능 카테고리 목록 조회
     */
    private List<CouponCategoryResponse> getApplicableCategories(Coupon coupon) {
        if (coupon.getApplicationType() != CouponApplicationType.CATEGORY) {
            return List.of();
        }

        List<CouponCategory> couponCategories = couponCategoryRepository.findByCouponIdWithCategory(coupon.getId());

        return couponCategories.stream()
                .map(cc -> CouponCategoryResponse.builder()
                        .categoryId(cc.getCategory().getId())
                        .categoryName(cc.getCategory().getName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 상품에 쿠폰 적용 가능한지 확인
     */
    private boolean isApplicableToProduct(Coupon coupon, Product product) {
        return switch (coupon.getApplicationType()) {
            case ALL -> true;
            case PRODUCT -> couponProductRepository.existsByCouponIdAndProductId(
                    coupon.getId(),
                    product.getId()
            );
            case CATEGORY -> {
                // 상품의 카테고리 ID 추출
                List<Long> productCategoryIds = product.getCategoryProducts().stream()
                        .map(cp -> cp.getCategory().getId())
                        .collect(Collectors.toList());

                // 쿠폰의 카테고리 ID 추출
                List<Long> couponCategoryIds = couponCategoryRepository.findByCouponIdWithCategory(coupon.getId())
                        .stream()
                        .map(cc -> cc.getCategory().getId())
                        .collect(Collectors.toList());

                // 교집합이 있으면 적용 가능
                yield productCategoryIds.stream()
                        .anyMatch(couponCategoryIds::contains);
            }
        };
    }

    /**
     * 쿠폰 생성 시 날짜 유효성 검증
     */
    private void validateCouponDates(CreateCouponRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_DATE);
            }
        }
    }

    /**
     * 쿠폰 수정 시 날짜 유효성 검증
     */
    private void validateUpdateCouponDates(UpdateCouponRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_DATE);
            }
        }
    }

    /**
     * 쿠폰 코드 대량 생성
     */
    private List<CouponUser> generateCouponCodes(Coupon coupon, int quantity, LocalDateTime expiresAt) {
        List<CouponUser> couponUsers = new ArrayList<>();
        Set<String> existingCodes = new HashSet<>();

        String prefix = coupon.getCodePrefix();

        for (int i = 0; i < quantity; i++) {
            String code = generateUniqueCouponCode(prefix, existingCodes);
            existingCodes.add(code);

            CouponUser couponUser = CouponUser.builder()
                    .coupon(coupon)
                    .code(code)
                    .expiresAt(expiresAt)
                    .status(CouponStatus.AVAILABLE)
                    .build();

            couponUsers.add(couponUser);
        }
        return couponUsers;
    }

    /**
     * 고유한 쿠폰 코드 생성
     */
    private String generateUniqueCouponCode(String prefix, Set<String> existingCodes) {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = CouponCodeGenerator.generate(prefix);
            attempts++;

            if (attempts >= maxAttempts) {
                throw new ServiceException(ServiceExceptionCode.COUPON_CODE_GENERATION_FAILED);
            }
        } while (existingCodes.contains(code) ||
                couponUserRepository.existsByCode(code));
        return code;
    }

    /**
     * 쿠폰 등록
     */
    @Transactional
    public RegisterCouponResponse registerCoupon(String couponCode, Account account) {
        // 1. 비관적 락으로 조회 (다른 트랜잭션이 접근 불가)
        CouponUser couponUser = couponUserRepository.findByCodeWithCouponForUpdate(couponCode)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.INVALID_COUPON_CODE));

        Long userId = account.getAccountId();

        // 2. 유효성 검증
        validateCouponRegistration(couponUser, userId);

        // 3. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        // 4. 쿠폰 등록
        couponUser.assignToUser(user);

        return couponMapper.toRegisterCouponResponse(couponUser);
    }

    /**
     * 쿠폰 등록 유효성 검증
     */
    private void validateCouponRegistration(CouponUser couponUser, Long userId) {
        // 1. status 확인: AVAILABLE이 아니면 등록 불가
        if (couponUser.getStatus() != CouponStatus.AVAILABLE) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_REGISTERED_COUPON);
        }

        // 2. user_id 확인: 이미 할당된 쿠폰인지 확인
        if (couponUser.getUser() != null) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_ASSIGNED_COUPON);
        }

        // 3. 만료 확인
        if (couponUser.isExpired()) {
            couponUser.expire();
            throw new ServiceException(ServiceExceptionCode.EXPIRED_COUPON);
        }

        // 4. 쿠폰 템플릿 유효 기간 확인
        if (!couponUser.getCoupon().isValid()) {
            throw new ServiceException(ServiceExceptionCode.INVALID_COUPON_PERIOD);
        }

        // 5. 중복 등록 방지
        boolean alreadyHas = couponUserRepository.existsByUserIdAndCouponId(
                userId,
                couponUser.getCoupon().getId()
        );
        if (alreadyHas) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_COUPON_REGISTRATION);
        }
    }

    /**
     * 결제 전 최대 할인 쿠폰 계산
     */
    public MaxDiscountCouponResponse calculateMaxDiscount(OrderProductsRequest request, Account account) {
        Long userId = account.getAccountId();

        // 1. 상품 정보 조회 및 총 금액 계산
        OrderCalculation calculation = calculateOrderAmount(request);

        // 2. 내 사용 가능한 쿠폰 조회 (할인순)
        List<CouponUser> myCoupons = couponUserQueryRepository.findAvailableByUserIdSorted(
                userId,
                CouponSortType.DISCOUNT_DESC
        );

        // 3. 각 쿠폰별 할인 금액 계산
        List<CouponDiscountResponse> applicableCoupons = myCoupons.stream()
                .map(couponUser -> calculateCouponDiscount(
                        couponUser,
                        calculation.getProducts(),
                        calculation.getTotalAmount()
                ))
                .filter(CouponDiscountResponse::getIsApplicable)
                .sorted((a, b) -> b.getDiscountAmount().compareTo(a.getDiscountAmount()))
                .collect(Collectors.toList());

        // 4. 최대 할인 쿠폰 선택
        CouponDiscountResponse bestCoupon = applicableCoupons.isEmpty()
                ? null
                : applicableCoupons.get(0);

        BigDecimal maxDiscountAmount = bestCoupon != null
                ? bestCoupon.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal finalAmount = calculation.getTotalAmount().subtract(maxDiscountAmount);

        return MaxDiscountCouponResponse.builder()
                .totalAmount(calculation.getTotalAmount())
                .maxDiscountAmount(maxDiscountAmount)
                .finalAmount(finalAmount)
                .bestCoupon(bestCoupon)
                .applicableCoupons(applicableCoupons)
                .build();
    }

    /**
     * 특정 쿠폰의 할인 금액 계산
     */
    public CouponDiscountResponse calculateCouponDiscount(Long couponUserId, OrderProductsRequest request, Account account) {
        // 1. 쿠폰 조회
        CouponUser couponUser = couponUserRepository.findByIdWithCoupon(couponUserId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON_USER));

        // 2. 본인 쿠폰인지 확인
        if (!couponUser.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.FORBIDDEN_COUPON_ACCESS);
        }

        // 3. 주문 금액 계산
        OrderCalculation calculation = calculateOrderAmount(request);

        // 4. 할인 금액 계산
        return calculateCouponDiscount(
                couponUser,
                calculation.getProducts(),
                calculation.getTotalAmount()
        );
    }

    /**
     * 주문 금액 계산 (상품 + 옵션 가격)
     */
    private OrderCalculation calculateOrderAmount(OrderProductsRequest request) {
        List<OrderProductRequest> orderProducts = request.getProducts();

        // 1. 상품 ID 추출
        List<Long> productIds = orderProducts.stream()
                .map(OrderProductRequest::getProductId)
                .collect(Collectors.toList());

        // 2. 상품 조회
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        if (productMap.size() != productIds.size()) {
            throw new ServiceException(ServiceExceptionCode.CANNOT_FOUND_PRODUCT);
        }

        // 3. 총 금액 계산 (옵션 가격 포함)
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> products = new ArrayList<>();

        for (OrderProductRequest item : orderProducts) {
            Product product = productMap.get(item.getProductId());
            products.add(product);
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

            // 옵션이 있는 경우
            if (item.getColor() != null || item.getSize() != null) {
                ProductOption option = productOptionRepository
                        .findByProductIdAndColorAndSize(
                                item.getProductId(),
                                item.getColor(),
                                item.getSize()
                        )
                        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.INVALID_PRODUCT_OPTION));

                // 옵션 가격 = 기본 가격 + 추가 가격
                BigDecimal basePrice = product.getPrice();
                BigDecimal additionalPrice = option.getAdditionalPrice();
                BigDecimal optionPrice = basePrice.add(additionalPrice);

                // 옵션 가격 * 수량
                BigDecimal itemTotal = optionPrice.multiply(quantity);

                // 총액에 더하기
                totalAmount = totalAmount.add(itemTotal);
            } else {
                // 옵션 없는 경우 기본 가격
                BigDecimal itemPrice = product.getPrice();
                BigDecimal itemTotal = itemPrice.multiply(quantity);

                totalAmount = totalAmount.add(itemTotal);
            }
        }

        return new OrderCalculation(products, totalAmount);
    }

    /**
     * 주문 시 쿠폰 사용 처리
     */
    @Transactional
    public void useCoupon(Long couponUserId, Long orderId, Account account) {
        // 1. 쿠폰 조회 (비관적 락)
        CouponUser couponUser = couponUserRepository.findByIdWithCouponForUpdate(couponUserId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON_USER));

        // 2. 본인 쿠폰인지 확인
        if (!couponUser.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.FORBIDDEN_COUPON_ACCESS);
        }

        // 3. 사용 가능 상태 확인
        if (couponUser.getStatus() != CouponStatus.ISSUED) {
            throw new ServiceException(ServiceExceptionCode.COUPON_NOT_AVAILABLE);
        }

        if (couponUser.isExpired()) {
            couponUser.expire();
            throw new ServiceException(ServiceExceptionCode.EXPIRED_COUPON);
        }

        // 4. 쿠폰 사용 처리
        couponUser.use(orderId);

        // 5. 쿠폰 템플릿의 사용 수 증가
        Coupon coupon = couponUser.getCoupon();
        coupon.increaseUsedCount();
    }

    /**
     * 쿠폰별 할인 금액 계산 (내부 로직)
     */
    private CouponDiscountResponse calculateCouponDiscount(
            CouponUser couponUser,
            List<Product> products,
            BigDecimal totalAmount
    ) {
        Coupon coupon = couponUser.getCoupon();

        // 1. 적용 가능 여부 확인
        if (!couponUser.isAvailable()) {
            return CouponDiscountResponse.notApplicable(
                    couponUser,
                    totalAmount,
                    "사용할 수 없는 쿠폰입니다"
            );
        }

        // 2. 최소 주문 금액 확인
        if (coupon.getMinOrderAmount() != null) {
            BigDecimal minAmount = BigDecimal.valueOf(coupon.getMinOrderAmount());

            if (totalAmount.compareTo(minAmount) < 0) {
                return CouponDiscountResponse.notApplicable(
                        couponUser,
                        totalAmount,
                        String.format("최소 주문 금액 %,d원 이상이어야 합니다", coupon.getMinOrderAmount())
                );
            }
        }

        // 3. 상품 적용 가능 여부 확인
        boolean isApplicable = products.stream()
                .anyMatch(product -> isApplicableToProduct(coupon, product));

        if (!isApplicable) {
            return CouponDiscountResponse.notApplicable(
                    couponUser,
                    totalAmount,
                    "해당 상품에는 적용할 수 없는 쿠폰입니다"
            );
        }

        // 4. 할인 금액 계산
        BigDecimal discountAmount = coupon.calculateDiscount(totalAmount);
        return CouponDiscountResponse.applicable(
                couponUser,
                totalAmount,
                discountAmount
        );
    }

    /**
     * 쿠폰 복구 (주문 취소 시)
     */
    @Transactional
    public void restoreCoupon(Long couponUserId) {
        CouponUser couponUser = couponUserRepository.findById(couponUserId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_COUPON_USER));

        // USED → ISSUED로 변경
        if (couponUser.getStatus() == CouponStatus.USED) {
            couponUser.restore();

            // 쿠폰 사용 수 감소
            Coupon coupon = couponUser.getCoupon();
            coupon.decreaseUsedCount();
        }
    }
}