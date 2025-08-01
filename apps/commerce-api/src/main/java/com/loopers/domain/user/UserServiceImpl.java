package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserEntity save(UserCommand.Create userCreateCommand) {
        if (userCreateCommand == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "User creation command cannot be null.");
        }

        try {
            return userRepository.save(userCreateCommand.toEntity());
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.CONFLICT, e.getMessage());
        }
    }

    @Override
    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }
}
