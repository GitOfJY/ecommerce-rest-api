package com.jy.shoppy.domain.auth.service;

import com.jy.shoppy.domain.auth.mapper.AccountMapper;
import com.jy.shoppy.domain.auth.mapper.AuthMapper;
import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.LoginResponse;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.auth.dto.RegisterUserResponse;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.UserGrade;
import com.jy.shoppy.domain.user.repository.UserGradeRepository;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserGradeRepository userGradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final AccountMapper accountMapper;
    private final HttpServletRequest request;

    @Transactional
    public RegisterUserResponse register(RegisterUserRequest req) {
        // 중복 이메일 확인
        validateDuplicateEmail(req.getEmail());

        // 등급 조회
        UserGrade grade = null;
        if (req.getRoleId() == 1) {
            grade = userGradeRepository.findByName("BRONZE")
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER_GRADE));
        } else if (req.getRoleId() == 2) {
            grade = userGradeRepository.findByName("ADMIN")
                    .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER_GRADE));
        }

        // 패스워드 인코딩
        String encodedPassword = passwordEncoder.encode(req.getPassword());
        User newUser = User.registerUser(req, encodedPassword, grade);
        userRepository.save(newUser);

        // 자동 로그인
        autoLogin(newUser);

        return accountMapper.toRegisterResponse(newUser);
    }

    // 중복 이메일 확인
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_USER_EMAIL);
        }
    }

    // 자동 로그인
    private void autoLogin(User user) {
        Account account = accountMapper.toAccount(user);
        log.info("Auto login for user {}", account);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        account,
                        null,
                        account.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 세션에 SecurityContext 저장
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    // 로그아웃
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // 로그인 상태 확인
    public LoginResponse getLoginInfo(Authentication authentication) {
        validateAuthentication(authentication);

        Account account = (Account) authentication.getPrincipal();
        User user = userRepository.findById(account.getAccountId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        return authMapper.toLoginResponse(user);
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new ServiceException(ServiceExceptionCode.NOT_AUTHENTICATED);
        }
    }
}
