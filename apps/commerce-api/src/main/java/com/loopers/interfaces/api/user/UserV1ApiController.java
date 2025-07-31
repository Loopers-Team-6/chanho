package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserV1ApiController implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.SignupRequest request
    ) {
        return ApiResponse.success(userFacade.signUp(request));
    }

    @PostMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> me(HttpServletRequest request) {
        if (request.getHeader("X-USER-ID") == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "User ID is missing in the request header.");
        }

        long userId = Long.parseLong(request.getHeader("X-USER-ID"));

        return ApiResponse.success(userFacade.find(userId));
    }

}
