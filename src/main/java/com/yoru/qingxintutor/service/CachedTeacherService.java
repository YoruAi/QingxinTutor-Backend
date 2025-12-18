package com.yoru.qingxintutor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class CachedTeacherService {
    public static final String TEACHER_ID_BY_USER_ID_PREFIX = "teacher:tid-by-uid:";
    public static final String USER_ID_BY_TEACHER_ID_PREFIX = "teacher:uid-by-tid:";
    public static final String NAME_BY_TEACHER_ID_PREFIX = "teacher:name-by-tid:";

    // TTL（秒）
    public static final int DEFAULT_TTL = 1800;
    public static final int NULL_TTL = 120;
    public static final String NULL_VALUE = "NULL";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 通用缓存读取方法（返回三态）
     */
    public Optional<Object> getFromCache(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        //noinspection ConstantValue
        if (value == null) {
            return Optional.empty();
        }
        if (NULL_VALUE.equals(value)) {
            return Optional.of(NULL_VALUE);
        }
        return Optional.of(value);
    }

    @Async
    public void putTeacherIdByUserId(String userId, Long teacherId) {
        redisTemplate.opsForValue().set(
                TEACHER_ID_BY_USER_ID_PREFIX + userId,
                teacherId,
                Duration.ofSeconds(DEFAULT_TTL)
        );
    }

    @Async
    public void cacheNullForTeacherIdByUserId(String userId) {
        redisTemplate.opsForValue().set(
                TEACHER_ID_BY_USER_ID_PREFIX + userId,
                NULL_VALUE,
                Duration.ofSeconds(NULL_TTL)
        );
    }

    @Async
    public void putUserIdByTeacherId(Long teacherId, String userId) {
        redisTemplate.opsForValue().set(
                USER_ID_BY_TEACHER_ID_PREFIX + teacherId,
                userId,
                Duration.ofSeconds(DEFAULT_TTL)
        );
    }

    @Async
    public void cacheNullForUserIdByTeacherId(Long teacherId) {
        redisTemplate.opsForValue().set(
                USER_ID_BY_TEACHER_ID_PREFIX + teacherId,
                NULL_VALUE,
                Duration.ofSeconds(NULL_TTL)
        );
    }

    @Async
    public void putNameById(Long teacherId, String name) {
        redisTemplate.opsForValue().set(
                NAME_BY_TEACHER_ID_PREFIX + teacherId,
                name,
                Duration.ofSeconds(DEFAULT_TTL)
        );
    }

    @Async
    public void cacheNullForNameById(Long teacherId) {
        redisTemplate.opsForValue().set(
                NAME_BY_TEACHER_ID_PREFIX + teacherId,
                NULL_VALUE,
                Duration.ofSeconds(NULL_TTL)
        );
    }

    // 清理缓存
    public void evictAllCachesByTeacherId(Long teacherId) {
        redisTemplate.delete(NAME_BY_TEACHER_ID_PREFIX + teacherId);
    }

    public void evictCacheByUserId(String userId) {
        redisTemplate.delete(TEACHER_ID_BY_USER_ID_PREFIX + userId);
    }
}
