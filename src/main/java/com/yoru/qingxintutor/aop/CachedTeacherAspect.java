package com.yoru.qingxintutor.aop;

import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import com.yoru.qingxintutor.service.CachedTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Aspect
@Component
public class CachedTeacherAspect {

    @Autowired
    private CachedTeacherService cachedTeacherService;

    // ========================
    // findTeacherIdByUserId
    // ========================
    @Around("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.findTeacherIdByUserId(String)) && args(userId)")
    public Object aroundFindTeacherIdByUserId(ProceedingJoinPoint joinPoint, String userId) throws Throwable {
        String key = CachedTeacherService.TEACHER_ID_BY_USER_ID_PREFIX + userId;
        Optional<Object> cached = cachedTeacherService.getFromCache(key);

        if (cached.isEmpty()) {
            @SuppressWarnings("unchecked")
            Optional<Long> fromDb = (Optional<Long>) joinPoint.proceed();

            if (fromDb.isPresent())
                cachedTeacherService.putTeacherIdByUserId(userId, fromDb.get());
            else
                cachedTeacherService.cacheNullForTeacherIdByUserId(userId);
            return fromDb;
        }

        Object value = cached.get();
        if (CachedTeacherService.NULL_VALUE.equals(value)) {
            return Optional.empty();
        } else if (value instanceof Number tid) {
            return Optional.of(tid.longValue());
        } else {
            log.error("Unexpected value in cache for key {} when findTeacherIdByUserId: {}", key, value);
            return Optional.empty();
        }
    }

    // ========================
    // findUserIdByTeacherId
    // ========================
    @Around("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.findUserIdByTeacherId(Long)) && args(teacherId)")
    public Object aroundFindUserIdByTeacherId(ProceedingJoinPoint joinPoint, Long teacherId) throws Throwable {
        String key = CachedTeacherService.USER_ID_BY_TEACHER_ID_PREFIX + teacherId;
        Optional<Object> cached = cachedTeacherService.getFromCache(key);

        if (cached.isEmpty()) {
            @SuppressWarnings("unchecked")
            Optional<String> fromDb = (Optional<String>) joinPoint.proceed();

            if (fromDb.isPresent())
                cachedTeacherService.putUserIdByTeacherId(teacherId, fromDb.get());
            else
                cachedTeacherService.cacheNullForUserIdByTeacherId(teacherId);
            return fromDb;
        }

        Object value = cached.get();
        if (CachedTeacherService.NULL_VALUE.equals(value)) {
            return Optional.empty();
        } else if (value instanceof String uid) {
            return Optional.of(uid);
        } else {
            log.error("Unexpected value in cache for key {} when findUserIdByTeacherId: {}", key, value);
            return Optional.empty();
        }
    }

    // ========================
    // findNameById
    // ========================
    @Around("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.findNameById(Long)) && args(teacherId)")
    public Object aroundFindNameById(ProceedingJoinPoint joinPoint, Long teacherId) throws Throwable {
        String key = CachedTeacherService.NAME_BY_TEACHER_ID_PREFIX + teacherId;
        Optional<Object> cached = cachedTeacherService.getFromCache(key);

        if (cached.isEmpty()) {
            @SuppressWarnings("unchecked")
            Optional<String> fromDb = (Optional<String>) joinPoint.proceed();

            if (fromDb.isPresent())
                cachedTeacherService.putNameById(teacherId, fromDb.get());
            else
                cachedTeacherService.cacheNullForNameById(teacherId);
            return fromDb;
        }

        Object value = cached.get();
        if (CachedTeacherService.NULL_VALUE.equals(value)) {
            return Optional.empty();
        } else if (value instanceof String name) {
            return Optional.of(name);
        } else {
            log.error("Unexpected value in cache for key {} when findNameById: {}", key, value);
            return Optional.empty();
        }
    }

    // ========================
    // 写操作：清理缓存
    // ========================

    @After("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.update(com.yoru.qingxintutor.pojo.entity.TeacherEntity)) && args(teacher)")
    public void afterUpdate(TeacherEntity teacher) {
        cachedTeacherService.evictAllCachesByTeacherId(teacher.getId());
    }

    @After("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.insert(com.yoru.qingxintutor.pojo.entity.TeacherEntity)) && args(teacher)")
    public void afterInsert(TeacherEntity teacher) {
        cachedTeacherService.evictAllCachesByTeacherId(teacher.getId());
        cachedTeacherService.evictCacheByUserId(teacher.getUserId());
    }

    @After(value = "execution(* com.yoru.qingxintutor.mapper.TeacherMapper.updateIconById(Long, String)) && args(id, icon)", argNames = "id,icon")
    public void afterUpdateIcon(Long id, String icon) {
        cachedTeacherService.evictAllCachesByTeacherId(id);
    }
}