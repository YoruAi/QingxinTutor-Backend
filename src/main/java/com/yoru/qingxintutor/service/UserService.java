package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.UserEmailMapper;
import com.yoru.qingxintutor.mapper.UserGithubMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateEmailRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdatePasswordRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateRequest;
import com.yoru.qingxintutor.pojo.entity.UserEmailEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.entity.UserGithubEntity;
import com.yoru.qingxintutor.pojo.result.UserInfoResult;
import com.yoru.qingxintutor.utils.EmailUtils;
import com.yoru.qingxintutor.utils.GithubOauthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AvatarService avatarService;
    @Autowired
    private UserEmailMapper emailMapper;
    @Autowired
    private UserGithubMapper githubMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private EmailUtils emailUtils;

    public UserInfoResult getInfo(String id) {
        UserEntity user = userMapper.findById(id).orElseThrow(() -> new BusinessException("User not found"));
        return UserInfoResult.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .icon(user.getIcon())
                .address(user.getAddress())
                .role(user.getRole())
                .auth(UserInfoResult.AuthInfo.builder()
                        .email(emailMapper.selectByUserId(user.getId())
                                .map(UserEmailEntity::getEmail)
                                .orElse(null))
                        .hasPassword(user.getPasswdHash() != null)
                        .githubId(githubMapper.selectByUserId(user.getId())
                                .map(UserGithubEntity::getSub)
                                .orElse(null))
                        .build())
                .build();
    }

    @Transactional
    public void updateInfo(String id, UserUpdateRequest request) {
        if (request.getUsername() != null) {
            Optional<UserEntity> user = userMapper.findByUsername(request.getUsername());
            if (user.isPresent() && !id.equals(user.get().getId())) {
                throw new BusinessException("Username has been registered");
            }
        }
        UserEntity updateUser = UserEntity.builder()
                .id(id)
                .username(request.getUsername())
                .nickname(request.getNickname())
                .address(request.getAddress())
                .build();
        userMapper.update(updateUser);
    }

    @Transactional
    public void updateAvatar(String id, String icon, String oldIcon) {
        if (!icon.startsWith("/avatar/"))
            throw new BusinessException("Avatar URL error, please contact admin");
        UserEntity updateUser = UserEntity.builder()
                .id(id)
                .icon(icon)
                .build();
        userMapper.update(updateUser);
        avatarService.deleteAvatar(oldIcon);
    }

    @Transactional
    public void updatePassword(String userId, UserUpdatePasswordRequest request) {
        if (request.getNewPassword() == null) {
            userMapper.updatePassword(userId, null);
            return;
        }

        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        // 1. 禁止新密码与旧密码相同
        if (user.getPasswdHash() != null && passwordEncoder.matches(request.getNewPassword(), user.getPasswdHash())) {
            throw new BusinessException("New password cannot be the same as old password");
        }

        // 2. 生成新密码哈希
        String newHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswdHash(newHash);
        request.setNewPassword(null);

        // 3. 更新数据库
        userMapper.updatePassword(userId, newHash);
    }

    @Transactional
    public void updateEmail(String userId, UserUpdateEmailRequest request) {
        verificationCodeService.attemptVerifyCode(request.getNewEmail(), request.getCode());
        emailMapper.deleteByUserId(userId);
        emailMapper.insert(UserEmailEntity.builder()
                .userId(userId)
                .email(request.getNewEmail().trim())
                .build());
        emailUtils.sendRegisterSuccess(request.getNewEmail().trim(), userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found")).getUsername());
    }

    @Transactional
    public void updateGithub(String userId, GithubOauthUtils.GithubUserInfo githubUserInfo) {
        githubMapper.deleteByUserId(userId);
        githubMapper.insert(UserGithubEntity.builder()
                .userId(userId)
                .sub(githubUserInfo.getId().toString())
                .build());
    }
}
