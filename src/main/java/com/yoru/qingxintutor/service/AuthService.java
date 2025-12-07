package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.*;
import com.yoru.qingxintutor.pojo.dto.request.*;
import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import com.yoru.qingxintutor.pojo.entity.UserEmailEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.entity.UserGithubEntity;
import com.yoru.qingxintutor.pojo.result.UserAuthResult;
import com.yoru.qingxintutor.utils.EmailUtils;
import com.yoru.qingxintutor.utils.GithubOauthUtils;
import com.yoru.qingxintutor.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserEmailMapper emailMapper;

    @Autowired
    private UserGithubMapper githubMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private TeacherSubjectMapper teacherSubjectMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private AvatarService avatarService;

    @Transactional
    public UserAuthResult registerStudent(UserRegisterRequest request) {
        // 0. 校验
        verificationCodeService.attemptVerifyCode(request.getEmail().trim(), request.getCode());
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (emailMapper.selectByEmail(request.getEmail().trim()).isPresent()) {
            throw new BusinessException("Email already registered");
        }

        // 1. 创建用户
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .passwdHash(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .role(UserEntity.Role.STUDENT)
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        request.setPassword(null);
        UserEmailEntity emailEntity = UserEmailEntity.builder()
                .userId(user.getId())
                .email(request.getEmail().trim())
                .build();
        emailMapper.insert(emailEntity);

        // 2. 创建钱包（核心附属数据）
        walletService.create(user.getId());

        // 注册邮件与欢迎通知
        emailUtils.sendRegisterSuccess(emailEntity.getEmail(), user.getUsername());
        notificationService.createPersonalNotification(user.getId(),
                "Welcome to Qingxin Tutor App",
                "Hello user " + user.getUsername() +
                        ". Now, you can search for excellent teachers and send reservation! " +
                        "Remember to complete your personal information as quickly as you can."
        );

        return createUserAuthResult(user);
    }

    @Transactional
    public UserAuthResult registerTeacher(TeacherRegisterRequest request) {
        // 0. 校验
        verificationCodeService.attemptVerifyCode(request.getUserRegisterRequest().getEmail(),
                request.getUserRegisterRequest().getCode());
        if (userMapper.findByUsername(request.getUserRegisterRequest().getUsername()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (emailMapper.selectByEmail(request.getUserRegisterRequest().getEmail()).isPresent()) {
            throw new BusinessException("Email already registered");
        }

        // 1. 创建用户
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUserRegisterRequest().getUsername())
                .passwdHash(request.getUserRegisterRequest().getPassword() != null ?
                        passwordEncoder.encode(request.getUserRegisterRequest().getPassword()) : null)
                .role(UserEntity.Role.TEACHER)
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        request.getUserRegisterRequest().setPassword(null);
        UserEmailEntity emailEntity = UserEmailEntity.builder()
                .userId(user.getId())
                .email(request.getUserRegisterRequest().getEmail().trim())
                .build();
        emailMapper.insert(emailEntity);

        // 2. 创建教师相关数据
        TeacherEntity teacher = TeacherEntity.builder()
                .userId(user.getId())
                .phone(request.getTeacherInfo().getPhone())
                .nickname(request.getTeacherInfo().getNickname())
                .name(request.getTeacherInfo().getName())
                .gender(request.getTeacherInfo().getGender())
                .birthDate(request.getTeacherInfo().getBirthDate())
                .address(request.getTeacherInfo().getAddress())
                .teachingExperience(request.getTeacherInfo().getTeachingExperience())
                .description(request.getTeacherInfo().getDescription())
                .grade(request.getTeacherInfo().getGrade())
                .build();
        teacherMapper.insert(teacher);
        // 创建科目信息
        List<String> subjectNames = request.getTeacherInfo().getSubjectNames();
        List<Long> subjectIds = new ArrayList<>();
        if (subjectNames != null && !subjectNames.isEmpty()) {
            List<String> validSubjects = subjectNames.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
            subjectIds = subjectMapper.findIdsByNames(validSubjects);
            if (subjectIds.size() != validSubjects.size()) {
                throw new BusinessException("One or more subjects do not exist");
            }
        }
        teacherSubjectMapper.batchInsert(teacher.getId(), subjectIds);

        // 注册邮件与欢迎通知
        emailUtils.sendRegisterSuccess(emailEntity.getEmail(), user.getUsername());
        notificationService.createPersonalNotification(user.getId(),
                "Welcome to Qingxin Tutor App",
                "Hello teacher " + user.getUsername() +
                        ". Now, you can manage your reservation and post in the forum! " +
                        "Remember to complete your personal information as quickly as you can."
        );

        return createUserAuthResult(user);
    }

    @Transactional
    public UserAuthResult passwordLogin(UserLoginPasswordRequest request) {
        UserEntity user;
        // 登录方式1：用户名 + 密码
        if (StringUtils.hasText(request.getUsername())) {
            user = userMapper.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"));
            if (user.getPasswdHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswdHash())) {
                throw new BusinessException("Invalid username/email or password");
            }
        }
        // 登录方式2：邮箱 + 密码
        else if (StringUtils.hasText(request.getEmail())) {
            String userId = emailMapper.selectByEmail(request.getEmail().trim())
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"))
                    .getUserId();
            user = userMapper.findById(userId)
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"));
            if (user.getPasswdHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswdHash())) {
                throw new BusinessException("Invalid username/email or password");
            }
        } else {
            throw new BusinessException("Invalid password login argument");
        }

        return createUserAuthResult(user);
    }

    @Transactional
    public UserAuthResult emailLogin(UserLoginEmailRequest request) {
        // 登录方式3：邮箱 + 验证码
        if (emailMapper.selectByEmail(request.getEmail().trim()).isEmpty()) {
            // 若未注册则自动注册（用户名为邮箱）
            return registerStudent(UserRegisterRequest.builder()
                    .username("email_" + request.getEmail().trim())
                    .password(null)
                    .email(request.getEmail())
                    .code(request.getCode())
                    .build());
        } else {
            verificationCodeService.attemptVerifyCode(request.getEmail().trim(), request.getCode());
            String userId = emailMapper.selectByEmail(request.getEmail().trim())
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"))
                    .getUserId();
            UserEntity user = userMapper.findById(userId)
                    .orElseThrow(() -> new BusinessException("Invalid username/email or password"));
            return createUserAuthResult(user);
        }
    }

    @Transactional
    public UserAuthResult githubLogin(GithubOauthUtils.GithubUserInfo githubUserInfo) {
        Integer githubUserId = githubUserInfo.getId();
        String githubUsername = githubUserInfo.getLogin();
        String avatarUrl = githubUserInfo.getAvatarUrl();
        Optional<String> optionalUserId = githubMapper.selectBySub(githubUserId.toString())
                .map(UserGithubEntity::getUserId);
        if (optionalUserId.isPresent()) {
            UserEntity user = userMapper.findById(optionalUserId.get())
                    .orElseThrow(() -> new BusinessException("User not found"));
            String token = jwtUtils.generateToken(user.getId());
            return UserAuthResult.builder()
                    .token(token)
                    .expireIn(jwtUtils.getJwtExpiration())
                    .user(UserAuthResult.AuthedUser.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .role(user.getRole())
                            .build())
                    .build();
        } else {
            LocalDateTime now = LocalDateTime.now();
            UserEntity user = UserEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .username(githubUsername)
                    .passwdHash(null)
                    .role(UserEntity.Role.STUDENT)
                    .icon(AvatarService.DEFAULT_AVATAR_URL)
                    .createTime(now)
                    .updateTime(now)
                    .build();
            userMapper.insert(user);
            // 保存github头像至本地
            avatarService.saveAvatarToLocal(githubUserId.toString(), avatarUrl)
                    .thenAccept(localUrl -> {
                        if (user.getIcon().equals(AvatarService.DEFAULT_AVATAR_URL)) {
                            userService.updateAvatar(user.getId(), localUrl);
                        }
                    });
            UserGithubEntity githubEntity = UserGithubEntity.builder()
                    .userId(user.getId())
                    .sub(githubUserId.toString())
                    .build();
            githubMapper.insert(githubEntity);

            // 2. 创建钱包（核心附属数据）
            walletService.create(user.getId());


            // 欢迎通知
            notificationService.createPersonalNotification(user.getId(),
                    "Welcome to Qingxin Tutor App",
                    "Hello user " + user.getUsername() +
                            ". Now, you can search for excellent teachers and send reservation! " +
                            "Remember to complete your personal information as quickly as you can."
            );

            return createUserAuthResult(user);
        }
    }

    @Transactional
    public void resetPassword(UserResetPasswordRequest request) {
        // 0. 校验
        verificationCodeService.attemptVerifyCode(request.getEmail().trim(), request.getCode());
        String userId = emailMapper.selectByEmail(request.getEmail().trim())
                .orElseThrow(() -> new BusinessException("Invalid email or password"))
                .getUserId();
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
        userMapper.updatePassword(user.getId(), newHash);
    }


    public UserAuthResult createUserAuthResult(UserEntity user) {
        // 生成 JWT 令牌（包含用户ID）
        String token = jwtUtils.generateToken(user.getId());
        return UserAuthResult.builder()
                .token(token)
                .expireIn(jwtUtils.getJwtExpiration())
                .user(UserAuthResult.AuthedUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build())
                .build();
    }

    public boolean hasEmail(String userId) {
        return emailMapper.selectByUserId(userId).isPresent();
    }

    public boolean hasGithub(String userId) {
        return githubMapper.selectByUserId(userId).isPresent();
    }

    public boolean hasPassword(String userId) {
        return userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getPasswdHash() != null;
    }
}
