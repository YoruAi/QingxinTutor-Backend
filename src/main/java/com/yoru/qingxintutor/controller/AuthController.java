package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.enums.EmailPurpose;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.UserLoginRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserRegisterRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserResetPasswordRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserSendCodeRequest;
import com.yoru.qingxintutor.pojo.dto.response.UserAuthResponse;
import com.yoru.qingxintutor.pojo.result.UserAuthResult;
import com.yoru.qingxintutor.service.UserService;
import com.yoru.qingxintutor.service.VerificationCodeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/register")
    public ApiResult<UserAuthResponse> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest)
            throws BusinessException {
        UserAuthResult userAuthResult = userService.register(userRegisterRequest);
        UserAuthResponse userAuthResponse = UserAuthResponse.builder()
                .expireIn(userAuthResult.getExpireIn())
                .token(userAuthResult.getToken())
                .user(UserAuthResponse.AuthedUser.builder()
                        .id(userAuthResult.getUserId())
                        .username(userAuthResult.getUsername())
                        .build())
                .build();
        return ApiResult.success(userAuthResponse);
    }

    @PostMapping("/login")
    public ApiResult<UserAuthResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest)
            throws BusinessException {
        UserAuthResult userAuthResult = userService.login(userLoginRequest);
        UserAuthResponse response = UserAuthResponse.builder()
                .token(userAuthResult.getToken())
                .expireIn(userAuthResult.getExpireIn())
                .user(UserAuthResponse.AuthedUser.builder()
                        .id(userAuthResult.getUserId())
                        .username(userAuthResult.getUsername())
                        .build())
                .build();
        return ApiResult.success(response);
    }

    @PostMapping("/reset-password")
    public ApiResult<UserAuthResponse> resetPassword(@Valid @RequestBody UserResetPasswordRequest userResetPasswordRequest)
            throws BusinessException {
        userService.resetPassword(userResetPasswordRequest);
        return ApiResult.success();
    }


    @PostMapping("/send-login-code")
    public ApiResult<String> sendLoginCode(@Valid @RequestBody UserSendCodeRequest userSendCodeRequest)
            throws BusinessException {
        verificationCodeService.sendVerificationCode(userSendCodeRequest.getEmail(), EmailPurpose.LOGIN);
        return ApiResult.success("If the email is valid, a verification code has been sent.");
    }

    @PostMapping("/send-register-code")
    public ApiResult<String> sendRegisterCode(@Valid @RequestBody UserSendCodeRequest userSendCodeRequest)
            throws BusinessException {
        verificationCodeService.sendVerificationCode(userSendCodeRequest.getEmail(), EmailPurpose.REGISTER);
        return ApiResult.success("If the email is valid, a verification code has been sent.");
    }

    @PostMapping("/send-reset-password-code")
    public ApiResult<String> sendResetPasswordCode(@Valid @RequestBody UserSendCodeRequest userSendCodeRequest)
            throws BusinessException {
        verificationCodeService.sendVerificationCode(userSendCodeRequest.getEmail(), EmailPurpose.RESET_PASSWORD);
        return ApiResult.success("If the email is valid, a verification code has been sent.");
    }
}
