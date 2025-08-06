package com.loopers.domain.user;

public interface UserService {

    UserEntity save(UserCommand.Create userCreateCommand);

    UserEntity findById(Long id);
}
