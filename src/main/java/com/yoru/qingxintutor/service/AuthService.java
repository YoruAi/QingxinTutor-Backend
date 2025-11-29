package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.SubjectMapper;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.TeacherSubjectMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.TeacherRegisterRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserLoginRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserRegisterRequest;
import com.yoru.qingxintutor.pojo.dto.request.UserResetPasswordRequest;
import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.result.UserAuthResult;
import com.yoru.qingxintutor.utils.EmailUtils;
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
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

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

    @Transactional
    public UserAuthResult registerStudent(UserRegisterRequest request) {
        // 0. 校验
        verificationCodeService.attemptVerifyCode(request.getEmail(), request.getCode());
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered");
        }

        // 1. 创建用户
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .passwdHash(passwordEncoder.encode(request.getPassword()))
                .role(UserEntity.Role.STUDENT)
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        request.setPassword(null);

        // 2. 创建钱包（核心附属数据）
        walletService.create(user.getId());

        // 3. 生成 JWT 令牌（通常包含用户ID）
        String token = jwtUtils.generateToken(user.getId());

        // 注册邮件与欢迎通知
        emailUtils.sendRegisterSuccess(user.getEmail(), user.getUsername());
        notificationService.createPersonalNotification(user.getId(),
                "Welcome to Qingxin Tutor App",
                "Hello user " + user.getUsername() +
                        ". Now, you can search for excellent teachers and send reservation! " +
                        "Remember to complete your personal information as quickly as you can."
        );

        return UserAuthResult.builder()
                .token(token)
                .expireIn(jwtUtils.getJwtExpiration())
                .user(UserAuthResult.AuthedUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(UserEntity.Role.STUDENT)
                        .build())
                .build();
    }

    @Transactional
    public UserAuthResult registerTeacher(TeacherRegisterRequest request) {
        // 0. 校验
        verificationCodeService.attemptVerifyCode(request.getUserRegisterRequest().getEmail(),
                request.getUserRegisterRequest().getCode());
        if (userMapper.findByUsername(request.getUserRegisterRequest().getUsername()).isPresent()) {
            throw new BusinessException("Username already taken");
        }
        if (userMapper.findByEmail(request.getUserRegisterRequest().getEmail()).isPresent()) {
            throw new BusinessException("Email already registered");
        }

        // 1. 创建用户
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUserRegisterRequest().getUsername())
                .email(request.getUserRegisterRequest().getEmail())
                .passwdHash(passwordEncoder.encode(request.getUserRegisterRequest().getPassword()))
                .role(UserEntity.Role.TEACHER)
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        request.getUserRegisterRequest().setPassword(null);

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
        // 更新科目信息
        // 获取请求中的新 subjectId 列表
        List<String> subjectNames = request.getTeacherInfo().getSubjectNames();
        List<Long> newSubjectIds = new ArrayList<>();
        if (subjectNames != null && !subjectNames.isEmpty()) {
            List<String> validSubjects = subjectNames.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
            newSubjectIds = subjectMapper.findIdsByNames(validSubjects);
            if (newSubjectIds.size() != validSubjects.size()) {
                throw new BusinessException("One or more subjects do not exist");
            }
        }
        teacherSubjectMapper.batchInsert(teacher.getId(), new ArrayList<>(newSubjectIds));

        // 3. 生成 JWT 令牌（通常包含用户ID）
        String token = jwtUtils.generateToken(user.getId());

        // 注册邮件与欢迎通知
        emailUtils.sendRegisterSuccess(user.getEmail(), user.getUsername());
        notificationService.createPersonalNotification(user.getId(),
                "Welcome to Qingxin Tutor App",
                "Hello teacher " + user.getUsername() +
                        ". Now, you can manage your reservation and post in the forum! " +
                        "Remember to complete your personal information as quickly as you can."
        );

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

    public UserAuthResult login(UserLoginRequest request) {
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

    public void resetPassword(UserResetPasswordRequest request) {
        // 0. 校验
        verificationCodeService.attemptVerifyCode(request.getEmail(), request.getCode());
        UserEntity user = userMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

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
