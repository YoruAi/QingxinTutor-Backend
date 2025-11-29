package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.enums.EmailPurpose;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.EmailVerificationCodeMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.entity.EmailVerificationCodeEntity;
import com.yoru.qingxintutor.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class VerificationCodeService {
    private static final int MAX_CODE_ATTEMPT_COUNT = 5;    // 验证码最多尝试次数
    private static final long SEND_COOLDOWN_SECONDS = 60;   // 60秒冷却
    private static final long CODE_EXPIRE_MINUTES = 3;      // 3分钟过期
    private static final Random random = new Random();

    @Autowired
    private EmailVerificationCodeMapper emailVerificationCodeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtils emailUtils;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attemptVerifyCode(String email, String inputCode) {
        // 1. 查验证码记录
        EmailVerificationCodeEntity codeRecord = emailVerificationCodeMapper.selectByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid or expired verification code"));

        // 2. 检查是否过期
        if (codeRecord.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Invalid or expired verification code");
        }

        // 3. 检查尝试次数（最多5次）
        if (codeRecord.getAttemptCount() >= MAX_CODE_ATTEMPT_COUNT) {
            throw new BusinessException("Maximum number of verification attempts exceeded");
        }

        // 4. 增加尝试次数
        codeRecord.setAttemptCount(codeRecord.getAttemptCount() + 1);
        emailVerificationCodeMapper.updateAttemptCountByEmail(email, codeRecord.getAttemptCount());

        // 5. 验证码是否正确
        if (!codeRecord.getCode().equals(inputCode)) {
            throw new BusinessException("Invalid or expired verification code");
        }

        // 6. 验证码正确则使验证码失效防止重放
        emailVerificationCodeMapper.deleteByEmail(email);
    }

    public void sendVerificationCode(String email, EmailPurpose purpose) {
        // 0. 注册用户必须未注册
        if (purpose == EmailPurpose.REGISTER && userMapper.findByEmail(email).isPresent()) {
            return;
        }
        // 1. 校验邮箱是否注册
        if (purpose != EmailPurpose.REGISTER && userMapper.findByEmail(email).isEmpty()) {
            return;
        }

        // 2. 查询最近一次发送记录
        LocalDateTime now = LocalDateTime.now();
        Optional<EmailVerificationCodeEntity> emailVerificationCodeEntity = emailVerificationCodeMapper.selectByEmail(email);
        if (emailVerificationCodeEntity.isPresent()) {
            if (emailVerificationCodeEntity.get().getCreateTime().plusSeconds(SEND_COOLDOWN_SECONDS).isAfter(now)) {
                return;
            }
        }

        // 3. 生成6位数字验证码
        String code = String.format("%06d", random.nextInt(1_000_000));

        // 4. 设置过期时间（3分钟后）
        LocalDateTime expireTime = now.plusMinutes(CODE_EXPIRE_MINUTES);

        // 5. 插入或更新数据库（覆盖旧记录）
        EmailVerificationCodeEntity record = EmailVerificationCodeEntity.builder()
                .email(email)
                .code(code)
                .attemptCount(0)
                .createTime(now)
                .expireTime(expireTime)
                .build();
        emailVerificationCodeMapper.upsert(record);

        // 6. 发送邮件
        if (purpose == EmailPurpose.LOGIN)
            emailUtils.sendLoginCode(email, code);
        else if (purpose == EmailPurpose.REGISTER)
            emailUtils.sendRegisterCode(email, code);
        else if (purpose == EmailPurpose.RESET_PASSWORD)
            emailUtils.sendResetPasswordCode(email, code);
        else
            log.error("Error email purpose {}", purpose);
    }
}
