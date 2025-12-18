package com.yoru.qingxintutor.aop;

import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.service.CachedUserService;
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
public class CachedUserAspect {
    @Autowired
    private CachedUserService cachedUserService;

    @Around("execution(* com.yoru.qingxintutor.mapper.UserMapper.findById(String)) && args(id)")
    public Object aroundFindById(ProceedingJoinPoint joinPoint, String id) throws Throwable {
        // 查缓存
        Optional<?> cached = cachedUserService.getFromCache(id);
        if (cached.isEmpty()) {
            // 缓存未命中，执行原方法（查 DB）
            @SuppressWarnings("unchecked")
            Optional<UserEntity> fromDb = (Optional<UserEntity>) joinPoint.proceed();

            // 回填缓存
            if (fromDb.isPresent())
                cachedUserService.putCache(id, fromDb.get());
            else
                cachedUserService.cacheNullFor(id);
            return fromDb;
        }
        Object value = cached.get();
        if (CachedUserService.NULL_VALUE.equals(value)) {
            return Optional.empty();
        } else if (value instanceof UserEntity user) {
            return Optional.of(user);
        } else {
            log.error("Unexpected value in cache: {}", value);
            return Optional.empty();
        }
    }

    @After("execution(* com.yoru.qingxintutor.mapper.UserMapper.update(com.yoru.qingxintutor.pojo.entity.UserEntity)) && args(user)")
    public void afterUpdate(UserEntity user) {
        cachedUserService.evictCache(user.getId());
    }

    @After("execution(* com.yoru.qingxintutor.mapper.UserMapper.insert(com.yoru.qingxintutor.pojo.entity.UserEntity)) && args(user)")
    public void afterInsert(UserEntity user) {
        cachedUserService.evictCache(user.getId());
    }

    @After(value = "execution(* com.yoru.qingxintutor.mapper.UserMapper.updatePassword(String, String)) && args(id, passwdHash)", argNames = "id,passwdHash")
    public void afterUpdate(String id, String passwdHash) {
        cachedUserService.evictCache(id);
    }
}
