package com.yoru.qingxintutor.aop;

import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import com.yoru.qingxintutor.pojo.result.TeacherInfoResult;
import com.yoru.qingxintutor.service.CachedTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Aspect
@Component
public class CachedTeacherAspect {
    @Autowired
    private CachedTeacherService cachedTeacherService;

    // ========================
    // findById
    // ========================
    @Around("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.findById(Long)) && args(id)")
    public Optional<TeacherInfoResult> aroundFindById(ProceedingJoinPoint joinPoint, Long id) throws Throwable {
        String key = CachedTeacherService.TEACHER_INFO_BY_ID_PREFIX + id;
        Optional<Object> cached = cachedTeacherService.getFromCache(key);
        if (cached.isEmpty()) {
            @SuppressWarnings("unchecked")
            Optional<TeacherInfoResult> fromDb = (Optional<TeacherInfoResult>) joinPoint.proceed();

            if (fromDb.isPresent())
                cachedTeacherService.putTeacherInfoById(id, fromDb.get());
            else
                cachedTeacherService.cacheNullForTeacherInfoById(id);
            return fromDb;
        }

        Object value = cached.get();
        if (CachedTeacherService.NULL_VALUE.equals(value)) {
            return Optional.empty();
        } else if (value instanceof TeacherInfoResult teacher) {
            return Optional.of(teacher);
        } else {
            log.error("Unexpected value in cache for key {} when findById: {}", key, value);
            return Optional.empty();
        }
    }

    // ========================
    // findByIds
    // ========================
    @Around("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.findByIds(java.util.List<Long>)) && args(ids)")
    public List<TeacherInfoResult> aroundFindById(ProceedingJoinPoint joinPoint, List<Long> ids) throws Throwable {
        if (ids.isEmpty())
            return List.of();

        // 查询缓存
        List<String> keys = ids.stream().map(id -> CachedTeacherService.TEACHER_INFO_BY_ID_PREFIX + id).toList();
        List<Optional<Object>> cachedList = cachedTeacherService.multiGetFromCache(keys);

        int size = ids.size();
        TeacherInfoResult[] results = new TeacherInfoResult[size];
        List<Integer> needLoadIndex = new ArrayList<>(size);
        List<Long> missingIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Optional<Object> cached = cachedList.get(i);
            if (cached.isPresent()) {
                // 命中缓存
                Object value = cached.get();
                if (CachedTeacherService.NULL_VALUE.equals(value)) {
                    results[i] = null;
                } else if (value instanceof TeacherInfoResult teacher) {
                    results[i] = teacher;
                } else {
                    log.error("Unexpected value in cache for key {} when findByIds: {}", keys.get(i), value);
                    results[i] = null;
                }
            } else {
                // 未命中缓存
                needLoadIndex.add(i);
                missingIds.add(ids.get(i));
            }
        }

        // 批量从 DB 查 missingIds
        if (!missingIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<TeacherInfoResult> dbResults = (List<TeacherInfoResult>) joinPoint.proceed(new Object[]{missingIds});
            for (int i = 0; i < dbResults.size(); i++) {
                TeacherInfoResult fromDb = dbResults.get(i);
                if (fromDb != null) {
                    results[needLoadIndex.get(i)] = fromDb;
                    cachedTeacherService.putTeacherInfoById(missingIds.get(i), fromDb);
                } else {
                    cachedTeacherService.cacheNullForTeacherInfoById(missingIds.get(i));
                }
            }
        }

        return Arrays.asList(results);
    }


    // ========================
    // findTeacherIdByUserId
    // ========================
    @Around("execution(* com.yoru.qingxintutor.mapper.TeacherMapper.findTeacherIdByUserId(String)) && args(userId)")
    public Optional<Long> aroundFindTeacherIdByUserId(ProceedingJoinPoint joinPoint, String userId) throws Throwable {
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
    public Optional<String> aroundFindUserIdByTeacherId(ProceedingJoinPoint joinPoint, Long teacherId) throws Throwable {
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
    public Optional<String> aroundFindNameById(ProceedingJoinPoint joinPoint, Long teacherId) throws Throwable {
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