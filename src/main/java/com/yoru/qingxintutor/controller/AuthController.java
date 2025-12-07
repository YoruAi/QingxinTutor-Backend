package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.*;
import com.yoru.qingxintutor.pojo.result.UserAuthResult;
import com.yoru.qingxintutor.service.AuthService;
import com.yoru.qingxintutor.service.VerificationCodeService;
import com.yoru.qingxintutor.utils.GithubOauthUtils;
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

    @Autowired
    private GithubOauthUtils githubOauthUtils;

    // 注册学生 - 邮箱验证码模式，密码可选
    @PostMapping("/register-user")
    public ApiResult<UserAuthResult> registerStudent(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserAuthResult userAuthResult = authService.registerStudent(userRegisterRequest);
        return ApiResult.success(userAuthResult);
    }

    // 注册老师 - 邮箱验证码模式，密码可选（注册为老师的唯一途径）
    @PostMapping("/register-teacher")
    public ApiResult<UserAuthResult> registerStudent(@Valid @RequestBody TeacherRegisterRequest teacherRegisterRequest) {
        UserAuthResult userAuthResult = authService.registerTeacher(teacherRegisterRequest);
        return ApiResult.success(userAuthResult);
    }

    // 密码登录 - 无密码将无法使用密码登录
    @PostMapping("/login/password")
    public ApiResult<UserAuthResult> login(@Valid @RequestBody UserLoginPasswordRequest userLoginPasswordRequest) {
        UserAuthResult userAuthResult = authService.passwordLogin(userLoginPasswordRequest);
        return ApiResult.success(userAuthResult);
    }

    // 邮箱登录 - 未注册用户若使用邮箱验证码模式可直接注册
    @PostMapping("/login/email")
    public ApiResult<UserAuthResult> loginEmail(@Valid @RequestBody UserLoginEmailRequest userLoginEmailRequest) {
        UserAuthResult userAuthResult = authService.emailLogin(userLoginEmailRequest);
        return ApiResult.success(userAuthResult);
    }

    // Github登录 - 未注册用户使用Github模式可直接注册
    @PostMapping("/login/github")
    public ApiResult<UserAuthResult> loginGithub(@Valid @RequestBody UserLoginGithubRequest userLoginGithubRequest) {
        GithubOauthUtils.GithubUserInfo githubUserInfo = githubOauthUtils
                .fetchGithubUserInfo(userLoginGithubRequest.getCode(), userLoginGithubRequest.getCode_verifier());
        UserAuthResult userAuthResult = authService.githubLogin(githubUserInfo);
        return ApiResult.success(userAuthResult);
    }


    // 忘记密码 - 邮箱验证码模式
    @PostMapping("/reset-password")
    public ApiResult<Void> resetPassword(@Valid @RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        authService.resetPassword(userResetPasswordRequest);
        return ApiResult.success();
    }

    // 发送验证码 - 每个验证码最多5次尝试次数，3分钟内有效；每个邮箱60秒最多发送一次验证码
    @PostMapping("/send-code")
    public ApiResult<String> sendCode(@Valid @RequestBody UserSendCodeRequest userSendCodeRequest) {
        verificationCodeService.sendVerificationCode(userSendCodeRequest.getEmail().trim(), userSendCodeRequest.getPurpose());
        return ApiResult.success("If the email is valid, a verification code has been sent.");
    }
}
