package com.jy.shoppy.domain.auth.service;

import com.jy.shoppy.domain.auth.dto.LoginResponse;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.user.entity.Role;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.UserGrade;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void init() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("정상적인 회원가입 성공")
        void register_success() {
            // given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("테스트유저")
                    .email("test@example.com")
                    .password("Password1!")
                    .phone("010-1234-5678")
                    .build();

            // when
            RegisterUserResponse response = authService.register(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("테스트유저");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getGradeName()).isEqualTo("BRONZE");

            // DB 저장 확인
            User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
            assertThat(savedUser.getUsername()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("회원가입 후 자동 로그인 확인")
        void register_autoLogin() {
            // given
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("테스트유저")
                    .email("test@example.com")
                    .password("Password1!")
                    .phone("010-1234-5678")
                    .build();

            // when
            authService.register(request);

            // then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 실패")
        void register_duplicateEmail_fail() {
            // given
            RegisterUserRequest request1 = RegisterUserRequest.builder()
                    .username("유저1")
                    .email("duplicate@example.com")
                    .password("Password1!")
                    .phone("010-1234-5678")
                    .build();

            RegisterUserRequest request2 = RegisterUserRequest.builder()
                    .username("유저2")
                    .email("duplicate@example.com")
                    .password("Password2!")
                    .phone("010-9876-5432")
                    .build();

            authService.register(request1);

            // when & then
            assertThatThrownBy(() -> authService.register(request2))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 성공 - SecurityContext 클리어")
        void logout_success() {
            // given
            setupAuthentication();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.getSession(true);

            // when
            authService.logout(request);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("로그아웃 성공 - 세션 무효화")
        void logout_sessionInvalidated() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute("testKey", "testValue");

            // when
            authService.logout(request);

            // then
            assertThatThrownBy(() -> session.getAttribute("testKey"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("세션 없이 로그아웃해도 에러 안남")
        void logout_noSession_noError() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();

            // when & then
            assertThatCode(() -> authService.logout(request))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("로그인 상태 확인 테스트")
    class GetLoginInfoTest {

        @Test
        @DisplayName("로그인 상태 확인 성공")
        void getLoginInfo_success() {
            // given
            User user = createAndSaveUser("loginuser@example.com");
            Authentication authentication = createAuthentication(user);

            // when
            LoginResponse response = authService.getLoginInfo(authentication);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("loginuser@example.com");
            assertThat(response.getGradeName()).isEqualTo("BRONZE");
        }

        @Test
        @DisplayName("인증 정보 없으면 실패")
        void getLoginInfo_noAuth_fail() {
            // when & then
            assertThatThrownBy(() -> authService.getLoginInfo(null))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("익명 사용자면 실패")
        void getLoginInfo_anonymous_fail() {
            // given
            Authentication anonymous = new org.springframework.security.authentication.AnonymousAuthenticationToken(
                    "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );

            // when & then
            assertThatThrownBy(() -> authService.getLoginInfo(anonymous))
                    .isInstanceOf(ServiceException.class);
        }
    }

    // 헬퍼 메서드
    private void setupAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User createAndSaveUser(String email) {
        User user = User.builder()
                .username("테스트유저")
                .email(email)
                .passwordHash("encodedPassword")
                .phone("01012345678")
                .role(Role.ref(1L))
                .userGrade(UserGrade.ref(1L))
                .build();
        return userRepository.save(user);
    }

    private Authentication createAuthentication(User user) {
        com.jy.shoppy.domain.auth.dto.Account account = com.jy.shoppy.domain.auth.dto.Account.builder()
                .accountId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().getName())))
                .build();

        return new UsernamePasswordAuthenticationToken(
                account, null, account.getAuthorities()
        );
    }

}