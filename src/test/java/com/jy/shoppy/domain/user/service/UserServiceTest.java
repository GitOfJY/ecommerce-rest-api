package com.jy.shoppy.domain.user.service;

import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.user.dto.UpdateUserRequest;
import com.jy.shoppy.domain.user.dto.UserResponse;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.type.UserStatus;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class UserServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    User admin;

    @BeforeEach
    void init() {
        // 기존 데이터 전부 삭제
        userRepository.deleteAll();

        // 관리자 계정 생성, 자동로그인
        RegisterUserRequest req = RegisterUserRequest.builder()
                .username("admin")
                .email("admin@test.com")
                .password("Test1234@")
                .phone("010-0000-0000")
                .roleId(2L)
                .build();

        RegisterUserResponse response = authService.register(req);
        Long userId = response.getId();
        admin = userRepository.findById(userId).get();
    }

    @Nested
    @DisplayName("[관리자] 테스트")
    class AdminUserTest {
        @Test
        @DisplayName("사용자 전체 조회")
        void admin_find_all_users() {
            // given - @BeforeEach admin 1 생성
            log.info("=== 테스트 시작 전 사용자 수: {}", userRepository.count());
            long beforeTotalCount = userRepository.count();

            RegisterUserRequest userReq = RegisterUserRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .password("Test1234@")
                    .phone("010-1111-1111")
                    .roleId(1L)  // 일반 사용자
                    .build();
            authService.register(userReq);

            // when
            List<UserResponse> findAllUsers = userService.findAllUsers();
            log.info("=== 조회된 사용자 수: {}", findAllUsers.size());
            long afterTotalCount = findAllUsers.size();

            // then
            assertThat(afterTotalCount).isEqualTo(beforeTotalCount + 1);
            assertThat(findAllUsers).isNotNull();
            assertThat(findAllUsers).hasSize(2);
            assertThat(findAllUsers)
                    .extracting(UserResponse::getEmail)
                    .contains("admin@test.com", "user1@test.com");
            assertThat(findAllUsers)
                    .allMatch(user -> user.getId() != null);
        }

        @Test
        @DisplayName("사용자 ID 조회")
        void admin_find_user_by_id() {
            // given
            RegisterUserRequest userReq = RegisterUserRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .password("Test1234@")
                    .phone("010-1111-1111")
                    .roleId(1L)  // 일반 사용자
                    .build();
            RegisterUserResponse user = authService.register(userReq);

            // when
            UserResponse response = userService.findById(user.getId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(user.getId());
            assertThat(response.getEmail()).isEqualTo("user1@test.com");
            assertThat(response.getUsername()).isEqualTo("user1");
        }

        @Test
        @DisplayName("사용자 ID 조회 - 존재하지 않는 ID")
        void admin_find_user_by_id_not_found() {
            // given
            Long invalidId = 99999L;

            // when & then
            assertThatThrownBy(() -> userService.findById(invalidId))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("사용자 사용자 수정")
        void admin_update_user() {
            // given
            RegisterUserRequest userReq = RegisterUserRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .password("Test1234@")
                    .phone("010-1111-1111")
                    .roleId(1L)  // 일반 사용자
                    .build();
            RegisterUserResponse user = authService.register(userReq);

            String newPassword = "Update1234!";
            UpdateUserRequest req = UpdateUserRequest.builder()
                    .id(user.getId())
                    .email("user1_edit@test.com")
                    .passwordHash(passwordEncoder.encode(newPassword))
                    .build();

            // when
            userService.update(req);
            User updateUser = userRepository.findById(user.getId()).get();

            // then
            assertThat(updateUser).isNotNull();
            assertThat(updateUser.getUsername()).isEqualTo("user1");
            assertThat(updateUser.getEmail()).isEqualTo("user1_edit@test.com");
            assertThat(updateUser.getPasswordHash()).isNotNull();
            assertThat(passwordEncoder.matches(newPassword, updateUser.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("사용자 사용자 수정 - 존재하지 않는 ID")
        void admin_update_user_not_found() {
            // given
            Long invalidId = 99999L;
            UpdateUserRequest req = UpdateUserRequest.builder()
                    .id(invalidId)
                    .email("user1_edit@test.com")
                    .build();

            // when & then
            assertThatThrownBy(() -> userService.update(req))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("사용자 삭제 - 주문 이력 없으면 완전 삭제")
        void admin_delete_user_without_orders() {
            // given
            RegisterUserRequest userReq = RegisterUserRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .password("Test1234@")
                    .phone("010-1111-1111")
                    .roleId(1L)
                    .build();
            RegisterUserResponse user = authService.register(userReq);
            Long userId = user.getId();

            // when
            userService.deleteById(userId);

            // then
            assertThat(userRepository.findById(userId)).isEmpty();
        }

        @Test
        @DisplayName("사용자 삭제 - 주문 이력 있으면 익명화")
        void admin_delete_user_with_orders() {
            // given
            RegisterUserRequest userReq = RegisterUserRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .password("Test1234@")
                    .phone("010-1111-1111")
                    .roleId(1L)
                    .build();
            RegisterUserResponse user = authService.register(userReq);
            Long userId = user.getId();
            User savedUser = userRepository.findById(userId).get();

            // 주문 생성
            Order order = Order.builder()
                    .user(savedUser)
                    .status(OrderStatus.COMPLETED)
                    .orderDate(LocalDateTime.now())
                    .totalPrice(BigDecimal.valueOf(10000))
                    .build();
            orderRepository.save(order);

            // when
            userService.deleteById(userId);

            // then
            User withdrawnUser = userRepository.findById(userId).get();
            assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
            assertThat(withdrawnUser.getUsername()).isEqualTo("탈퇴회원");
            assertThat(withdrawnUser.getEmail()).isEqualTo("withdrawn_" + userId + "@deleted.com");
            assertThat(withdrawnUser.getPhone()).isEqualTo("00000000000");
            assertThat(withdrawnUser.getPasswordHash()).isEmpty();
            assertThat(withdrawnUser.getWithdrawnAt()).isNotNull();
        }

        @Test
        @DisplayName("사용자 삭제 - 존재하지 않는 ID")
        void admin_delete_user_not_found() {
            // given
            Long invalidId = 99999L;

            // when & then
            assertThatThrownBy(() -> userService.deleteById(invalidId))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("사용자 삭제 - 이미 탈퇴한 사용자")
        void admin_delete_user_already_withdrawn() {
            // given
            RegisterUserRequest userReq = RegisterUserRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .password("Test1234@")
                    .phone("010-1111-1111")
                    .roleId(1L)
                    .build();
            RegisterUserResponse user = authService.register(userReq);
            Long userId = user.getId();

            // 첫 번째 삭제 - 완전 삭제
            userService.deleteById(userId);

            // when & then - 다시 삭제 시도 (이미 없는 사용자)
            assertThatThrownBy(() -> userService.deleteById(userId))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("[사용자] 테스트")
    class UserTest {

    }
}