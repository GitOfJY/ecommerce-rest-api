package com.jy.shoppy.domain.auth.service;

import com.jy.shoppy.domain.auth.AuthMapper;
import com.jy.shoppy.domain.auth.dto.Account;
import com.jy.shoppy.domain.auth.dto.LoginResponse;
import com.jy.shoppy.domain.auth.dto.RegisterUserRequest;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    @Transactional
    public void register(RegisterUserRequest req) {
        // 중복 이메일 확인
        validateDuplicateEmail(req.getEmail());

        // 패스워드 인코딩
        String encodedPassword = passwordEncoder.encode(req.getPasswordHash());

        User newUser = User.registerUser(req, encodedPassword);
        userRepository.save(newUser);
    }

    // 중복 이메일 확인
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_USER_EMAIL);
        }
    }

    public LoginResponse getLoginInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceException(ServiceExceptionCode.NOT_AUTHENTICATED);
        }
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Account account)) {
            throw new ServiceException(ServiceExceptionCode.INVALID_AUTH_PRINCIPAL);
        }

        return authMapper.toLoginResponse(account);
    }
}
