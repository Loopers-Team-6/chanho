package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FakeUserRepository implements UserRepository {

    private final Map<Long, UserEntity> userRepository = new ConcurrentHashMap<>();

    @Override
    public UserEntity save(UserEntity userEntity) {
        UserEntity user = userRepository.putIfAbsent(userEntity.getId(), userEntity);
        return user == null ? userEntity : user;
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return Optional.ofNullable(userRepository.get(id));
    }
}
