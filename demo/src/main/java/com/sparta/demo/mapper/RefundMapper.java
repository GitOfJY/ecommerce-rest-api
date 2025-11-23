package com.sparta.demo.mapper;

import com.sparta.demo.entity.Refund;
import com.sparta.demo.service.dto.CreateRefundResponse;
import com.sparta.demo.service.dto.RefundResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefundMapper {
    CreateRefundResponse toResponse(Refund refund);

    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "refundId", source = "id")
    RefundResponse toResponseByUserId(Refund refund);

    List<RefundResponse> toResponseList(List<Refund> refunds);
}
