package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.enums.EmailPurpose;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.UserEmailMapper;
import com.yoru.qingxintutor.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeService {
    private static final Random random = new Random();

    private static final int CODE_EXPIRE_SECONDS = 180;
    private static final int SEND_COOLDOWN_SECONDS = 60;
    private static final int SEND_COOLDOWN_DAILY_SECONDS = 86400;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int MAX_SEND_DAILY_ATTEMPTS = 50;

    private static final String CODE_KEY_PREFIX = "auth:email:code:";
    private static final String VERIFY_COUNT_PREFIX = "auth:email:verify:count:";
    private static final String EMAIL_COOLDOWN_PREFIX = "auth:email:cooldown:";
    private static final String EMAIL_RATE_24H_PREFIX = "auth:email:rate24h:";
    private static final String IP_RATE_24H_PREFIX = "auth:ip:rate24h:";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserEmailMapper emailMapper;
    @Autowired
    private EmailUtils emailUtils;

    /**
     * 验证邮箱验证码（失败抛出业务异常）
     */
    public void attemptVerifyCode(String email, String inputCode) {
        // 获取正确验证码
        String correctCode = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
        //noinspection ConstantValue
        if (correctCode == null || !redisTemplate.hasKey(VERIFY_COUNT_PREFIX + email))
            throw new BusinessException("Invalid or expired verification code");
        Long verifyCount = redisTemplate.opsForValue().increment(VERIFY_COUNT_PREFIX + email);
        // 检查该验证码是否已被验证超过5次
        if (verifyCount > MAX_VERIFY_ATTEMPTS) {
            throw new BusinessException("The verification code has been attempted too many times");
        }
        // 匹配验证码
        if (!correctCode.equals(inputCode)) {
            throw new BusinessException("Invalid or expired verification code");
        }

        // 验证成功，删除验证码（防重放）
        redisTemplate.delete(CODE_KEY_PREFIX + email);
        redisTemplate.delete(VERIFY_COUNT_PREFIX + email);
    }

    public void sendVerificationCode(String email, String ip, EmailPurpose purpose) {
        boolean emailExists = emailMapper.selectByEmail(email).isPresent();
        // 0. 注册用户必须未注册
        if (purpose == EmailPurpose.REGISTER && emailExists)
            return;
        // 0. 重置密码用户校验邮箱是否注册
        if (purpose == EmailPurpose.RESET_PASSWORD && !emailExists)
            return;

        // 1. 检查邮箱60秒冷却
        if (Boolean.TRUE.equals(redisTemplate.hasKey(EMAIL_COOLDOWN_PREFIX + email))) {
            throw new BusinessException("Please wait 60 seconds before requesting again");
        }
        // 1. 检查邮箱24小时发送次数
        String emailDailyCountKey = EMAIL_RATE_24H_PREFIX + email;
        if (redisTemplate.hasKey(emailDailyCountKey)) {
            long emailCount = redisTemplate.opsForValue().increment(emailDailyCountKey);
            if (emailCount > MAX_SEND_DAILY_ATTEMPTS)
                throw new BusinessException("Reached the daily verification limit");
        }
        // 1. 检查IP 24小时发送次数
        String ipDailyCountKey = IP_RATE_24H_PREFIX + ip;
        if (redisTemplate.hasKey(ipDailyCountKey)) {
            long ipCount = redisTemplate.opsForValue().increment(ipDailyCountKey);
            if (ipCount > MAX_SEND_DAILY_ATTEMPTS)
                throw new BusinessException("Reached the daily verification limit");
        }

        // 2. 生成6位数字验证码
        String code = String.format("%06d", random.nextInt(1_000_000));

        // 3. 发送邮件
        switch (purpose) {
            case LOGIN -> emailUtils.sendLoginCode(email, code);
            case REGISTER -> emailUtils.sendRegisterCode(email, code);
            case RESET_PASSWORD -> emailUtils.sendResetPasswordCode(email, code);
        }

        // 4. 设置验证码过期与尝试次数
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + email, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(VERIFY_COUNT_PREFIX + email, "0", CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        // 5. 设置60秒冷却（邮箱）
        redisTemplate.opsForValue().set(EMAIL_COOLDOWN_PREFIX + email, "1", SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
        // 6. 设置24h发送次数限制（邮箱和IP）
        redisTemplate.opsForValue().setIfAbsent(emailDailyCountKey, "1", SEND_COOLDOWN_DAILY_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().setIfAbsent(ipDailyCountKey, "1", SEND_COOLDOWN_DAILY_SECONDS, TimeUnit.SECONDS);
    }
}
