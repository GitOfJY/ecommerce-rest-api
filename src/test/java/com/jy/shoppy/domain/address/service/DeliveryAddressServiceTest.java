package com.jy.shoppy.domain.address.service;

import com.jy.shoppy.domain.address.dto.DeliveryAddressRequest;
import com.jy.shoppy.domain.address.dto.DeliveryAddressResponse;
import com.jy.shoppy.domain.address.entity.DeliveryAddress;
import com.jy.shoppy.domain.address.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DeliveryAddressServiceTest {
    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private UserRepository userRepository;

    private Account account;
    private User user;

    @BeforeEach
    void init() {
        user = User.builder()
                .email("test123@test.com")
                .passwordHash("password123")
                .username("테스트유저")
                .phone("010-1234-5678")
                .build();
        userRepository.save(user);

        account = Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .build();
    }

    private DeliveryAddressRequest createRequest(String alias, boolean isDefault) {
        return DeliveryAddressRequest.builder()
                .recipientName("test")
                .recipientPhone("010-1111-2222")
                .recipientEmail("test123@test.com")
                .zipCode("12345")
                .city("서울시")
                .street("강남구 테헤란로 123")
                .detail("101동 202호")
                .alias(alias)
                .isDefault(isDefault)
                .build();
    }

    @Test
    @DisplayName("첫 번째 배송지는 기본 배송지 설정")
    void addFirstAddress_shouldBeDefault() {
        // given
        DeliveryAddressRequest request = createRequest("집", false);

        // when
        DeliveryAddressResponse response = deliveryAddressService.addAddress(account, request);

        // then
        assertThat(response.isDefault()).isTrue();
        assertThat(response.getRecipientName()).isEqualTo("test");
        assertThat(response.getAlias()).isEqualTo("집");
        assertThat(response.getFullAddress()).contains("서울시", "강남구 테헤란로 123");
    }

    @Test
    @DisplayName("두 번째 배송지를 기본으로 등록하면 기존 기본 배송지 해제")
    void addSecondAddressAsDefault_shouldClearExistingDefault() {
        // given
        deliveryAddressService.addAddress(account, createRequest("집", false));
        DeliveryAddressRequest secondRequest = createRequest("회사", true);

        // when
        DeliveryAddressResponse response = deliveryAddressService.addAddress(account, secondRequest);

        // then
        assertThat(response.isDefault()).isTrue();
        assertThat(response.getAlias()).isEqualTo("회사");

        List<DeliveryAddress> addresses = deliveryAddressRepository.findByUserIdOrderByIsDefaultDesc(user.getId());
        long defaultCount = addresses.stream().filter(DeliveryAddress::isDefault).count();
        assertThat(defaultCount).isEqualTo(1);
    }

    @Test
    @DisplayName("두 번째 배송지를 기본이 아닌 것으로 등록하면 기존 기본 배송지 유지")
    void addSecondAddressNotDefault_shouldKeepExistingDefault() {
        // given
        deliveryAddressService.addAddress(account, createRequest("집", false));
        DeliveryAddressRequest secondRequest = createRequest("회사", false);

        // when
        DeliveryAddressResponse response = deliveryAddressService.addAddress(account, secondRequest);

        // then
        assertThat(response.isDefault()).isFalse();

        List<DeliveryAddressResponse> addresses = deliveryAddressService.getAddresses(account);
        DeliveryAddressResponse defaultAddress = addresses.stream()
                .filter(DeliveryAddressResponse::isDefault)
                .findFirst()
                .orElseThrow();
        assertThat(defaultAddress.getAlias()).isEqualTo("집");
    }

    @Test
    @DisplayName("존재하지 않는 유저로 배송지 등록 시 예외 발생")
    void addAddress_userNotFound_shouldThrowException() {
        // given
        Account invalidAccount = Account.builder()
                .accountId(9999L)
                .email("invalid@test.com")
                .build();
        DeliveryAddressRequest request = createRequest("집", false);

        // when & then
        assertThatThrownBy(() -> deliveryAddressService.addAddress(invalidAccount, request))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("유저의 배송지 목록을 기본 배송지 우선으로 조회")
    void getAddresses_shouldReturnListOrderedByDefault() {
        // given
        deliveryAddressService.addAddress(account, createRequest("집", false));
        deliveryAddressService.addAddress(account, createRequest("회사", false));
        deliveryAddressService.addAddress(account, createRequest("부모님댁", true));

        // when
        List<DeliveryAddressResponse> responses = deliveryAddressService.getAddresses(account);

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).isDefault()).isTrue();
        assertThat(responses.get(0).getAlias()).isEqualTo("부모님댁");
    }

    @Test
    @DisplayName("배송지가 없으면 빈 목록 반환")
    void getAddresses_noAddresses_shouldReturnEmptyList() {
        // when
        List<DeliveryAddressResponse> responses = deliveryAddressService.getAddresses(account);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("기본 배송지 변경")
    void updateDefault_shouldChangeDefault() {
        // given
        DeliveryAddressResponse first = deliveryAddressService.addAddress(account, createRequest("집", false));
        DeliveryAddressResponse second = deliveryAddressService.addAddress(account, createRequest("회사", false));

        // when
        deliveryAddressService.updateDefault(account, second.getId());

        // then
        List<DeliveryAddressResponse> addresses = deliveryAddressService.getAddresses(account);
        DeliveryAddressResponse newDefault = addresses.stream()
                .filter(DeliveryAddressResponse::isDefault)
                .findFirst()
                .orElseThrow();
        assertThat(newDefault.getAlias()).isEqualTo("회사");
    }

    @Test
    @DisplayName("존재하지 않는 배송지로 기본 변경 시 예외 발생")
    void updateDefault_addressNotFound_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> deliveryAddressService.updateDefault(account, 9999L))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("다른 유저의 배송지를 기본으로 변경 시 예외 발생")
    void updateDefault_unauthorized_shouldThrowException() {
        // given
        User otherUser = User.builder()
                .email("other@test.com")
                .passwordHash("password123")
                .username("다른유저")
                .phone("010-9999-9999")
                .build();
        userRepository.save(otherUser);

        Account otherAccount = Account.builder()
                .accountId(otherUser.getId())
                .email(otherUser.getEmail())
                .build();

        DeliveryAddressResponse otherAddress = deliveryAddressService.addAddress(otherAccount, createRequest("다른집", false));

        // when & then
        assertThatThrownBy(() -> deliveryAddressService.updateDefault(account, otherAddress.getId()))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("배송지 삭제")
    void deleteAddress_shouldDelete() {
        // given
        DeliveryAddressResponse response = deliveryAddressService.addAddress(account, createRequest("집", false));

        // when
        deliveryAddressService.deleteAddress(account, response.getId());

        // then
        assertThat(deliveryAddressService.getAddresses(account)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 배송지 삭제 시 예외 발생")
    void deleteAddress_addressNotFound_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> deliveryAddressService.deleteAddress(account, 9999L))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("다른 유저의 배송지를 삭제 시 예외 발생")
    void deleteAddress_unauthorized_shouldThrowException() {
        // given
        User otherUser = User.builder()
                .email("other@test.com")
                .passwordHash("password123")
                .username("다른유저")
                .phone("010-9999-9999")
                .build();
        userRepository.save(otherUser);

        Account otherAccount = Account.builder()
                .accountId(otherUser.getId())
                .email(otherUser.getEmail())
                .build();

        DeliveryAddressResponse otherAddress = deliveryAddressService.addAddress(otherAccount, createRequest("다른집", false));

        // when & then
        assertThatThrownBy(() -> deliveryAddressService.deleteAddress(account, otherAddress.getId()))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("기본 배송지 조회")
    void getDefaultAddress_shouldReturn() {
        // given
        deliveryAddressService.addAddress(account, createRequest("집", false));
        deliveryAddressService.addAddress(account, createRequest("회사", true));

        // when
        DeliveryAddress result = deliveryAddressService.getDefaultAddress(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
        assertThat(result.getAlias()).isEqualTo("회사");
    }

    @Test
    @DisplayName("기본 배송지가 없으면 null 반환")
    void getDefaultAddress_noDefault_shouldReturnNull() {
        // when
        DeliveryAddress result = deliveryAddressService.getDefaultAddress(user.getId());

        // then
        assertThat(result).isNull();
    }
}