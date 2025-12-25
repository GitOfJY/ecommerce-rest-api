package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.order.dto.GuestOrderCompleteRequest;
import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.mapper.OrderMapper;
import com.jy.shoppy.domain.order.repository.OrderQueryRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.domain.user.service.UserGradeService;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final UserGradeService userGradeService;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    /**
     * 주문 상세 조회
     */
    public OrderResponse findById(Long orderId) {
        Order order = orderRepository.findByIdWithProducts(orderId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        return orderMapper.toResponse(order);
    }

    /**
     * 전체 주문 조회
     */
    public List<OrderResponse> findAll() {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponseList(orders);
    }

    /**
     * 관리자 - 주문 완료 처리
     * 회원 주문인 경우 구매금액 누적 및 등급 자동 승급
     */
    @Transactional
    public OrderResponse complete(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));

        // 주문 완료 처리
        order.complete();

        // 회원 주문인 경우 구매금액 업데이트 및 등급 체크
        if (order.getUser() != null) {
            User user = order.getUser();
            user.updatePurchaseAmount(order.getTotalPrice());
            userGradeService.checkAndUpgradeGrade(user);
            userRepository.save(user);

            log.info("주문 완료 처리 및 회원 정보 업데이트 완료: orderId={}, userId={}, totalAmount={}",
                    orderId, user.getId(), user.getTotalPurchaseAmount());
        }

        orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));
        // 주문 취소
        order.cancel();
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> searchOrdersPage(SearchOrderCond cond, Pageable pageable) {
        Page<Order> page = orderQueryRepository.searchOrdersPage(cond, pageable);
        return page.map(orderMapper::toResponse);
    }
}