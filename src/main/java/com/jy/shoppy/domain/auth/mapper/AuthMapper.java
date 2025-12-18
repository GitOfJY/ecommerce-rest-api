package com.jy.shoppy.domain.auth.mapper;

import com.jy.shoppy.domain.auth.dto.LoginResponse;

import com.jy.shoppy.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.name", target = "role")
    @Mapping(source = "userGrade.name", target = "gradeName")
    @Mapping(source = "userGrade.discountRate", target = "discountRate")
    LoginResponse toLoginResponse(User user);
}
