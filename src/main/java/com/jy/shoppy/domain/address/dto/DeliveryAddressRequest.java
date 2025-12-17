package com.jy.shoppy.domain.address.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddressRequest {
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;

    private String zipCode;
    private String city;
    private String street;
    private String detail;

    private String alias;

    private boolean isDefault;
}
