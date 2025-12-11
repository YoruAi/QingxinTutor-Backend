package com.yoru.qingxintutor.annotation;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.utils.IPUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class AntiReplayAspect {
    private static final String ANTI_REPLAY_PREFIX = "nonce:";
    private static final long ALLOWED_TIME_WINDOW_MS = 60_000; // 1分钟
    private static final long NONCE_TTL_SECONDS = 120;         // 2分钟

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Around("@annotation(antiReplay)")
    public Object checkAntiReplay(ProceedingJoinPoint joinPoint, AntiReplay antiReplay) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();
        String ip = IPUtils.getClientIpAddress(request);

        // 1. 获取请求头
        String nonce = request.getHeader("X-Nonce");
        String timestampStr = request.getHeader("X-Timestamp");
        if (!StringUtils.hasText(nonce) || timestampStr == null)
            throw new BusinessException("Missing X-Nonce or X-Timestamp header");

        if (nonce.length() < 32)
            throw new BusinessException("Invalid X-Nonce");
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid X-Timestamp format");
        }

        // 2. 校验时间窗口
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > ALLOWED_TIME_WINDOW_MS) {
            throw new BusinessException("Request expired");
        }

        // 3. Redis 检查 nonce 是否已使用
        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(ANTI_REPLAY_PREFIX + ":" + ip + ":" + nonce, "1", NONCE_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(isFirst)) {
            throw new BusinessException("Duplicate request (replayed)");
        }

        // 4. 放行
        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //noinspection ConstantValue
        if (attrs == null)
            throw new BusinessException("HTTP request error");
        return attrs.getRequest();
    }
}
