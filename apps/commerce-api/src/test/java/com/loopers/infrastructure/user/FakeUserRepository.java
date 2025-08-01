package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;

public class FakeUserRepository extends InMemoryCrudRepository<UserEntity> implements UserRepository {
}
