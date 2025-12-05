package com.jy.shoppy.service;

import com.jy.shoppy.common.ServiceException;
import com.jy.shoppy.common.ServiceExceptionCode;
import com.jy.shoppy.entity.User;
import com.jy.shoppy.mapper.UserMapper;
import com.jy.shoppy.repository.UserQueryRepository;
import com.jy.shoppy.repository.UserRepository;
import com.jy.shoppy.service.dto.CreateUserRequest;
import com.jy.shoppy.service.dto.UpdateUserRequest;
import com.jy.shoppy.service.dto.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserMapper userMapper;


    // 회원 가입
    @Transactional
    public Long create(CreateUserRequest user) {
        // 중복 이메일 확인
        validateDuplicateEmail(user.getEmail());

        User newUser = User.createUser(user);
        userRepository.save(newUser);

        return newUser.getId();
    }

    // 중복 이메일 확인
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(ServiceExceptionCode.DUPLICATE_USER_EMAIL);
        }
    }

    // 회원 전체 조회
    public List<UserResponse> findAllUsers() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    // 회원 ID 단건 조회
    public UserResponse findById(Long id) {
        User findUser = userQueryRepository.getUserById(id)
                .orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        return userMapper.toResponse(findUser);
    }

    // 회원 수정
    @Transactional
    public Long update(UpdateUserRequest req) {
        User findUser = userRepository.findById(req.getId()).orElseThrow(() -> new ServiceException(ServiceExceptionCode.CANNOT_FOUND_USER));
        findUser.updateUser(req);
        return findUser.getId();
    }

    // 회원 삭제
    public void deleteById(Long id) {
        // TODO : 주문 이력이 있으면 삭제 불가 > 휴면 계정으로 변화
        userRepository.deleteById(id);
    }
}
