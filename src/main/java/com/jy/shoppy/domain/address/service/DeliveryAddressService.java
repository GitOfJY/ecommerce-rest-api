package com.jy.shoppy.domain.address.service;

import com.jy.shoppy.domain.address.dto.DeliveryAddressRequest;
import com.jy.shoppy.domain.address.dto.DeliveryAddressResponse;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.address.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final UserRepository userRepository;

    public DeliveryAddressResponse addAddress(Account account, DeliveryAddressRequest request) {
        User user = userRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        boolean isFirst = !deliveryAddressRepository.existsByUserId(account.getAccountId());
        boolean shouldBeDefault = request.isDefault() || isFirst;

        if (shouldBeDefault) {
            deliveryAddressRepository.findByUserIdAndIsDefaultTrue(account.getAccountId())
                    .ifPresent(existingDefault -> existingDefault.updateIsDefault(false));
        }
        DeliveryAddress deliveryAddress = DeliveryAddress.createDeliveryAddress(user, request, shouldBeDefault);
        deliveryAddressRepository.save(deliveryAddress);

        return DeliveryAddressResponse.from(deliveryAddress);
    }

    public void updateDefault(Account account, Long deliveryAddressId) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_DELIVERY_ADDRESS));

        if (!deliveryAddress.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED);
        }

        deliveryAddressRepository.clearDefaultByUserId(account.getAccountId());
        deliveryAddressRepository.updateDefaultTrue(deliveryAddressId);
    }

    @Transactional(readOnly = true)
    public List<DeliveryAddressResponse> getAddresses(Account account) {
        return deliveryAddressRepository.findByUserIdOrderByIsDefaultDesc(account.getAccountId())
                .stream()
                .map(DeliveryAddressResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeliveryAddress getDefaultAddress(Long userId) {
        return deliveryAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElse(null);
    }

    public void deleteAddress(Account account, Long deliveryAddressId) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressId)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_DELIVERY_ADDRESS));

        if (!deliveryAddress.getUser().getId().equals(account.getAccountId())) {
            throw new ServiceException(ServiceExceptionCode.UNAUTHORIZED);
        }
        deliveryAddressRepository.delete(deliveryAddress);
    }
}
