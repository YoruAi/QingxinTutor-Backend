package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateEmailRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateGithubRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdatePasswordRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateRequest;
import com.yoru.qingxintutor.pojo.result.UserInfoResult;
import com.yoru.qingxintutor.service.AvatarService;
import com.yoru.qingxintutor.service.UserService;
import com.yoru.qingxintutor.utils.GithubOauthUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;
    @Autowired
    private GithubOauthUtils githubOauthUtils;

    // User Info
    @GetMapping("/me")
    public ApiResult<UserInfoResult> getProfiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    @PutMapping("/me")
    public ApiResult<UserInfoResult> updateProfiles(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        userService.updateInfo(userDetails.getUser().getId(), userUpdateRequest);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    // upload avatar
    @PostMapping("/upload-avatar")
    public ApiResult<UserInfoResult> uploadAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam("file") MultipartFile file) {
        String accessURL = avatarService.uploadAvatar(file);
        userService.updateAvatar(userDetails.getUser().getId(), accessURL);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    // 重设密码；无密码用户可通过该接口设置密码（登录态无需认证）；若新密码为null则为去除密码登录
    @PostMapping("/update-password")
    public ApiResult<UserInfoResult> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest) {
        userService.updatePassword(userDetails.getUser().getId(), userUpdatePasswordRequest);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    // 重设邮箱；无邮箱用户可通过该接口设置邮箱（登录态无需认证，只需认证新邮箱，使用auth中接口发送验证码，目的为REGISTER）
    @PostMapping("/update-email")
    public ApiResult<UserInfoResult> updateEmail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @Valid @RequestBody UserUpdateEmailRequest userUpdateEmailRequest) {
        userService.updateEmail(userDetails.getUser().getId(), userUpdateEmailRequest);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    // 重设github；无github用户可通过该接口设置github
    @PostMapping("/update-github")
    public ApiResult<UserInfoResult> updateGithub(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @Valid @RequestBody UserUpdateGithubRequest userUpdateGithubRequest) {
        GithubOauthUtils.GithubUserInfo githubUserInfo = githubOauthUtils
                .fetchGithubUserInfo(userUpdateGithubRequest.getCode(), userUpdateGithubRequest.getCode_verifier());
        userService.updateGithub(userDetails.getUser().getId(), githubUserInfo);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }
}
