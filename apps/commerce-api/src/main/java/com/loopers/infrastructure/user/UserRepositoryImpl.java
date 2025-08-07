package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.AbstractRepositoryImpl;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryImpl extends AbstractRepositoryImpl<UserEntity, UserJpaRepository> implements UserRepository {
    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        super(jpaRepository);
    }
}
