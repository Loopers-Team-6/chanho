package com.loopers.application.user;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.point.PointV1Dto;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final PointService pointService;

    @Transactional(readOnly = true)
    public UserV1Dto.UserResponse find(Long id) {
        UserEntity user = userService.findById(id);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "User not found with ID: [%s]".formatted(id));
        }

        return UserV1Dto.UserResponse.from(user);
    }

    @Transactional
    public UserV1Dto.UserResponse signUp(UserV1Dto.SignupRequest request) {
        UserCommand.Create create = new UserCommand.Create(
                request.username(),
                request.email(),
                request.gender(),
                request.birth()
        );

        UserEntity saved = userService.save(create);
        PointEntity point = pointService.save(new PointEntity(saved));

        return UserV1Dto.UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PointV1Dto.PointResponse findBalance(Long userId) {
        PointEntity point = pointService.findByUserId(userId);
        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "Point not found for user ID: [%s]".formatted(userId));
        }

        return PointV1Dto.PointResponse.from(point);
    }

    @Transactional
    public PointV1Dto.PointResponse charge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Charge amount must be greater than zero.");
        }

        UserEntity user = userService.findById(userId);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "User not found with ID: [%s]".formatted(userId));
        }

        PointEntity point = pointService.findByUserId(userId);
        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "Point not found for user ID: [%s]".formatted(userId));
        }
        point.charge(amount);

        return PointV1Dto.PointResponse.from(pointService.save(point));
    }
}
