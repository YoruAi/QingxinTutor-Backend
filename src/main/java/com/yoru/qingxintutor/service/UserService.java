package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.UserLoginRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserRegisterRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserResetPasswordRequest;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.result.UserAuthResult;
import com.yoru.qingxintutor.utils.EmailUtils;
import com.yoru.qingxintutor.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Transactional
    public UserAuthResult register(UserRegisterRequest request) throws BusinessException {
        // 0. 校验
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered");
        }
        verificationCodeService.attemptVerifyCode(request.getEmail(), request.getCode());

        // 1. 创建用户
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .passwdHash(passwordEncoder.encode(request.getPassword()))
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        request.setPassword(null);

        // 2. 创建钱包（核心附属数据）
        // TODO: 用户创建附属数据

        // 3. 生成 JWT 令牌（通常包含用户ID）
        String token = jwtUtil.generateToken(user.getId());

        // 4. （可选）发送欢迎邮件、初始化学习计划等...
        // 注册邮件
        emailUtils.sendRegisterSuccess(user.getEmail(), user.getUsername());

        return UserAuthResult.builder()
                .token(token)
                .expireIn(jwtUtil.getJwtExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public UserAuthResult login(UserLoginRequest request) throws BusinessException {
        UserEntity user;
        // 方式1：用户名 + 密码
        if (StringUtils.hasText(request.getUsername())) {
            user = userMapper.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"));
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswdHash())) {
                throw new BusinessException("Password error");
            }
        }
        // 方式2或3：通过邮箱
        else if (StringUtils.hasText(request.getEmail())) {
            user = userMapper.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"));

            // 方式2：邮箱 + 密码
            if (StringUtils.hasText(request.getPassword())) {
                if (!passwordEncoder.matches(request.getPassword(), user.getPasswdHash())) {
                    throw new BusinessException("Invalid username/email or password");
                }
            }
            // 方式3：邮箱 + 验证码
            else if (StringUtils.hasText(request.getCode())) {
                verificationCodeService.attemptVerifyCode(request.getEmail(), request.getCode());
            } else {
                throw new BusinessException("Invalid login argument");
            }

        } else {
            throw new BusinessException("Invalid login argument");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId());

        return UserAuthResult.builder()
                .token(token)
                .expireIn(jwtUtil.getJwtExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public void resetPassword(UserResetPasswordRequest request) throws BusinessException {
        // 0. 校验
        UserEntity user = userMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid username/email or password"));
        verificationCodeService.attemptVerifyCode(request.getEmail(), request.getCode());

        // 1. 禁止新密码与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswdHash())) {
            throw new BusinessException("New password cannot be the same as old password");
        }

        // 2. 生成新密码哈希
        String newHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswdHash(newHash);
        request.setNewPassword(null);

        // 3. 更新数据库
        userMapper.updatePassword(user.getId(), newHash);
    }
}
