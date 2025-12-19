package com.jy.shoppy.domain.user.service;

import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.auth.service.AuthService;
import com.jy.shoppy.domain.order.entity.Order;
import com.jy.shoppy.domain.order.entity.type.OrderStatus;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.user.dto.LoginIdRequest;
import com.jy.shoppy.domain.user.dto.LoginPasswordRequest;
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
        orderRepository.deleteAll();
        userRepository.deleteAll();

        admin = createTestUser("admin", "admin@test.com", "010-0000-0000", 2L);
    }

    @Nested
    @DisplayName("[관리자] 테스트")
    class AdminUserTest {

        @Test
        @DisplayName("사용자 전체 조회")
        void admin_find_all_users() {
            // given
            long beforeCount = userRepository.count();
            createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);

            // when
            List<UserResponse> findAllUsers = userService.findAllUsers();

            // then
            assertThat(findAllUsers).hasSize((int) beforeCount + 1);
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
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);

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
        @DisplayName("사용자 수정")
        void admin_update_user() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);

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
            assertThat(updateUser.getUsername()).isEqualTo("user1");
            assertThat(updateUser.getEmail()).isEqualTo("user1_edit@test.com");
            assertThat(passwordEncoder.matches(newPassword, updateUser.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("사용자 수정 - 존재하지 않는 ID")
        void admin_update_user_not_found() {
            // given
            UpdateUserRequest req = UpdateUserRequest.builder()
                    .id(99999L)
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
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);
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
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);
            Long userId = user.getId();
            createTestOrder(user);

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
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);
            Long userId = user.getId();
            createTestOrder(user);

            userService.deleteById(userId);

            // when & then
            assertThatThrownBy(() -> userService.deleteById(userId))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("[사용자] 테스트")
    class UserTest {

        @Test
        @DisplayName("이름, 휴대폰으로 이메일 조회")
        void user_find_email_by_username_and_phone() {
            // given
            createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);

            LoginIdRequest req = LoginIdRequest.builder()
                    .username("user1")
                    .phone("01011111111")
                    .build();

            // when
            String email = userService.findEmail(req);

            // then
            assertThat(email).isEqualTo("use***@test.com");
        }

        @Test
        @DisplayName("이메일 조회 - 존재하지 않는 사용자")
        void user_find_email_not_found() {
            // given
            LoginIdRequest req = LoginIdRequest.builder()
                    .username("없는사용자")
                    .phone("01011111111")
                    .build();

            // when & then
            assertThatThrownBy(() -> userService.findEmail(req))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("이메일로 임시 비밀번호 발급")
        void user_request_password_reset_by_email() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);

            LoginPasswordRequest request = LoginPasswordRequest.builder()
                    .username("user1")
                    .email("user1@test.com")
                    .build();

            // when
            String tmpPassword = userService.sendTemporaryPassword(request);

            // then
            assertThat(tmpPassword).isNotNull();
            assertThat(tmpPassword).hasSize(10);

            User updatedUser = userRepository.findById(user.getId()).get();
            assertThat(passwordEncoder.matches(tmpPassword, updatedUser.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("휴대폰 번호로 임시 비밀번호 발급")
        void user_request_password_reset_by_phone() {
            // given
            User user = createTestUser("user1", "user1@test.com", "010-1111-1111", 1L);

            LoginPasswordRequest request = LoginPasswordRequest.builder()
                    .username("user1")
                    .phone("01011111111")
                    .build();

            // when
            String tmpPassword = userService.sendTemporaryPassword(request);

            // then
            assertThat(tmpPassword).isNotNull();
            assertThat(tmpPassword).hasSize(10);

            User updatedUser = userRepository.findById(user.getId()).get();
            assertThat(passwordEncoder.matches(tmpPassword, updatedUser.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("임시 비밀번호 발급 - 존재하지 않는 사용자")
        void user_request_password_reset_not_found() {
            // given
            LoginPasswordRequest request = LoginPasswordRequest.builder()
                    .username("없는사용자")
                    .email("notfound@test.com")
                    .build();

            // when & then
            assertThatThrownBy(() -> userService.sendTemporaryPassword(request))
                    .isInstanceOf(ServiceException.class);
        }
    }

    private User createTestUser(String username, String email, String phone, Long roleId) {
        RegisterUserRequest req = RegisterUserRequest.builder()
                .username(username)
                .email(email)
                .password("Test1234@")
                .phone(phone)
                .roleId(roleId)
                .build();
        RegisterUserResponse response = authService.register(req);
        return userRepository.findById(response.getId()).get();
    }

    private void createTestOrder(User user) {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(10000))
                .build();
        orderRepository.save(order);
    }
}