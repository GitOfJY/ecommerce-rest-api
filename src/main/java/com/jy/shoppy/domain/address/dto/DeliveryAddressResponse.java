package com.jy.shoppy.domain.address.dto;

import com.jy.shoppy.domain.address.entity.Address;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryAddressResponse {

    private Long id;

    private String recipientName;

    private String recipientPhone;

    private String recipientEmail;

    private String zipCode;

    private String city;

    private String street;

    private String detail;

    private String fullAddress;

    private String alias;

    private boolean isDefault;

    public static DeliveryAddressResponse from(DeliveryAddress deliveryAddress) {
        Address address = deliveryAddress.getAddress();

        return DeliveryAddressResponse.builder()
                .id(deliveryAddress.getId())
                .recipientName(deliveryAddress.getRecipientName())
                .recipientPhone(deliveryAddress.getRecipientPhone())
                .recipientEmail(deliveryAddress.getRecipientEmail())
                .zipCode(address.getZipCode())
                .city(address.getCity())
                .street(address.getStreet())
                .detail(address.getDetail())
                .fullAddress(address.getFullAddress())
                .alias(deliveryAddress.getAlias())
                .isDefault(deliveryAddress.isDefault())
                .build();
    }
}