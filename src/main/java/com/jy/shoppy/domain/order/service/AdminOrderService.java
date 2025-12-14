package com.jy.shoppy.domain.order.service;

import com.jy.shoppy.domain.order.dto.OrderResponse;
import com.jy.shoppy.domain.order.dto.SearchOrderCond;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.mapper.OrderMapper;
import com.jy.shoppy.domain.order.repository.OrderQueryRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
