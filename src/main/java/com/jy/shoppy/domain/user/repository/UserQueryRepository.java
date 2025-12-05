package com.jy.shoppy.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.jy.shoppy.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.jy.shoppy.domain.order.entity.QOrder.order;
import static com.jy.shoppy.domain.user.entity.QUser.user;


@Repository
@RequiredArgsConstructor
public class UserQueryRepository {
    private final JPAQueryFactory queryFactory;
    public Optional<User> getUserById(Long id) {
        User foundUser = queryFactory  // 변수명 변경
                .selectFrom(user)
                .leftJoin(user.orders, order).fetchJoin()
                .where(user.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(foundUser);
    }
}
