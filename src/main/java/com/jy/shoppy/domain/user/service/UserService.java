package com.jy.shoppy.domain.user.service;

import com.jy.shoppy.domain.address.repository.DeliveryAddressRepository;
import com.jy.shoppy.domain.order.repository.OrderRepository;
import com.jy.shoppy.domain.user.dto.LoginIdRequest;
import com.jy.shoppy.domain.user.dto.LoginPasswordRequest;
import com.jy.shoppy.domain.user.entity.User;
import com.jy.shoppy.domain.user.entity.type.UserStatus;
import com.jy.shoppy.domain.user.mapper.UserMapper;
import com.jy.shoppy.domain.user.repository.UserQueryRepository;
import com.jy.shoppy.domain.user.repository.UserRepository;
import com.jy.shoppy.domain.user.dto.UpdateUserRequest;
import com.jy.shoppy.domain.user.dto.UserResponse;
import com.jy.shoppy.global.exception.ServiceException;
import com.jy.shoppy.global.exception.ServiceExceptionCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원 전체 조회
    public List<UserResponse> findAllUsers() {
        // 탈퇴 회원 제외
        return userMapper.toResponseList(userRepository.findAllByStatusNot(UserStatus.WITHDRAWN));
    }

    // 회원 ID 단건 조회
    public UserResponse findById(Long id) {
        User findUser = userQueryRepository.getUserById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        if (findUser.isWithdrawn()) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_WITHDRAWN_USER);
        }

        return userMapper.toResponse(findUser);
    }

    // 회원 수정
    @Transactional
    public Long update(UpdateUserRequest req) {
        User findUser = userRepository.findById(req.getId())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        if (findUser.isWithdrawn()) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_WITHDRAWN_USER);
        }

        findUser.updateUser(req);
        return findUser.getId();
    }

    // 회원 삭제
    @Transactional
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        if (user.isWithdrawn()) {
            throw new ServiceException(ServiceExceptionCode.ALREADY_WITHDRAWN_USER);
        }

        // 배송지 삭제 (공통)
        deliveryAddressRepository.deleteAllByUserId(id);

        boolean hasOrders = orderRepository.existsByUserId(id);
        if (hasOrders) {
            // 주문 이력 있으면 익명화
            user.anonymize();
        } else {
            // 주문 이력 없으면 완전 삭제
            userRepository.delete(user);
        }
    }

    public String findEmail(LoginIdRequest request) {
        User findUser = userRepository.findByUsernameAndPhone(request.getUsername(), request.getPhone())
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        return maskEmail(findUser.getEmail());
    }

    /**
     * 이메일 마스킹
     * 예: test@gmail.com -> tes***@gmail.com
     */
    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return email;

        String username = parts[0];
        String maskedUsername = username.substring(0, Math.min(3, username.length())) + "***";
        return maskedUsername + "@" + parts[1];
    }

    // 또는 임시 비밀번호 발급 방식
    public String sendTemporaryPassword(LoginPasswordRequest request) {
        User user = userRepository.findByEmailOrPhoneWithName(
                request.getEmail(),
                request.getPhone(),
                request.getUsername()
        ).orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));

        // 임시 비밀번호 생성
        String tempPassword = generateTemporaryPassword();

        // 해싱 후 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(tempPassword));

        // TODO : 이메일로 임시 비밀번호 전송
        // emailService.sendTemporaryPassword(user.getEmail(), tempPassword);

        return tempPassword;
    }

    /**
     * 임시 비밀번호 생성
     * 규칙: 영문 대소문자 + 숫자 조합 10자리
     */
    private String generateTemporaryPassword() {
        // 보안 강화: 특수문자 포함
        String upperCase = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCase = RandomStringUtils.random(4, 97, 122, true, true);
        String digits = RandomStringUtils.randomNumeric(3);
        String specialChars = RandomStringUtils.random(1, "!@#$%^&*");

        String combined = upperCase + lowerCase + digits + specialChars;

        // 셔플
        List<Character> chars = combined.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars);

        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
