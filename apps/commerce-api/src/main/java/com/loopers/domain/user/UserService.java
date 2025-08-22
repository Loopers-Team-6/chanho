package com.loopers.domain.user;

import com.loopers.application.user.UserCommand;

public interface UserService {

    UserEntity save(UserCommand.Create userCreateCommand);

    UserEntity findById(Long id);
}
