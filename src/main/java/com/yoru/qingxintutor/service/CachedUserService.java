package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.pojo.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class CachedUserService {
    public static final String USER_CACHE_PREFIX = "user:id:";
    
    // TTL（秒）
    public static final int DEFAULT_TTL = 1800;
    public static final int NULL_TTL = 120;
    public static final String NULL_VALUE = "NULL";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 从 Redis 获取用户信息
     *
     * @return Optional
     * - empty()        → 未命中
     * - of(NULL_VALUE) → 命中空缓存
     * - of(UserEntity) → 命中有效用户
     */
    public Optional<?> getFromCache(String id) {
        Object value = redisTemplate.opsForValue().get(USER_CACHE_PREFIX + id);
        //noinspection ConstantValue
        if (value == null)
            return Optional.empty();
        if (NULL_VALUE.equals(value)) {
            return Optional.of(NULL_VALUE);
        }
        if (value instanceof UserEntity user)
            return Optional.of(user);
        return Optional.empty();
    }

    /**
     * 手动写入缓存
     */
    @Async
    public void putCache(String id, UserEntity user) {
        redisTemplate.opsForValue().set(USER_CACHE_PREFIX + id, user, Duration.ofSeconds(DEFAULT_TTL));
    }

    @Async
    public void cacheNullFor(String id) {
        redisTemplate.opsForValue().set(USER_CACHE_PREFIX + id, NULL_VALUE, Duration.ofSeconds(NULL_TTL));
    }

    /**
     * 清理用户缓存（在新增/更新用户时调用）
     */
    public void evictCache(String userId) {
        redisTemplate.delete(USER_CACHE_PREFIX + userId);
    }
}
