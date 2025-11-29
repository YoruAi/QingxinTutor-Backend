package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.*;
import com.yoru.qingxintutor.pojo.result.UserAuthResult;
import com.yoru.qingxintutor.service.AuthService;
import com.yoru.qingxintutor.service.VerificationCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/register-user")
    public ApiResult<UserAuthResult> registerStudent(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserAuthResult userAuthResult = authService.registerStudent(userRegisterRequest);
        return ApiResult.success(userAuthResult);
    }

    @PostMapping("/register-teacher")
    public ApiResult<UserAuthResult> registerStudent(@Valid @RequestBody TeacherRegisterRequest teacherRegisterRequest) {
        UserAuthResult userAuthResult = authService.registerTeacher(teacherRegisterRequest);
        return ApiResult.success(userAuthResult);
    }

    @PostMapping("/login")
    public ApiResult<UserAuthResult> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        UserAuthResult userAuthResult = authService.login(userLoginRequest);
        return ApiResult.success(userAuthResult);
    }

    @PostMapping("/reset-password")
    public ApiResult<Void> resetPassword(@Valid @RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        authService.resetPassword(userResetPasswordRequest);
        return ApiResult.success();
    }

    @PostMapping("/send-code")
    public ApiResult<String> sendCode(@Valid @RequestBody UserSendCodeRequest userSendCodeRequest) {
        verificationCodeService.sendVerificationCode(userSendCodeRequest.getEmail(), userSendCodeRequest.getPurpose());
        return ApiResult.success("If the email is valid, a verification code has been sent.");
    }
}
