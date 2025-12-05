package com.jy.shoppy.domain.refund.service;

import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.refund.dto.CreateRefundRequest;
import com.jy.shoppy.domain.refund.dto.CreateRefundResponse;
import com.jy.shoppy.domain.refund.dto.RefundResponse;
import com.jy.shoppy.domain.refund.dto.UpdateRefundRequest;
import com.jy.shoppy.domain.refund.repository.RefundQueryRepository;
import com.jy.shoppy.domain.refund.repository.RefundRepository;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.refund.entity.Refund;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.refund.entity.type.RefundStatus;
import com.jy.shoppy.domain.refund.mapper.RefundMapper;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;
    private final UserRepository userRepository;
    private final RefundQueryRepository refundQueryRepository;

    // 환불 요청
    @Transactional
    public CreateRefundResponse create(CreateRefundRequest req) {
        // 환불 요청 주문 확인
        Order order = orderRepository.findById(req.getOrderId()).orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_ORDER));

        // 환불 생성
        Refund refund = Refund.createRefund(req, order);

        // 환불 저장
        refundRepository.save(refund);
        return refundMapper.toResponse(refund);
    }

    // 환불 처리
    @Transactional
    public Long updateRefund(Long refundId, UpdateRefundRequest req) {
        // 관리자 확인
        User checkedAdmin = userRepository.findById(req.getAdminId()).orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        // TODO USER_ROLDE enum으로 변경
        if (!"ROLE_ADMIN".equals(checkedAdmin.getRole().getName())) {
            throw new ServiceException(ServiceExceptionCode.ADMIN_ONLY_REFUND_PROCESS);
        }

        // 환불건 확인
        Refund findRefund = refundRepository.findById(refundId).orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_REFUND));

        // 환불요청 승인/거절 선택
        findRefund.updateRefund(req, checkedAdmin);

        // 승인시 상품재고 복원 및 주문 상태 업데이트
        if (req.getRefundStatus() == RefundStatus.APPROVED) {
            findRefund.getOrder().cancel();
        }

        return refundId;
    }

    // 환불 조회
    public Page<RefundResponse> searchRefunds(Long userId, RefundStatus status, Pageable pageable) {
        Page<Refund> page = refundQueryRepository.searchRefundByUserIdPage(userId, status, pageable);
        return page.map(refundMapper::toResponseByUserId);
    }
}
