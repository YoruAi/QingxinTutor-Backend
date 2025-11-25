package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.EmailVerificationCodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface EmailVerificationCodeMapper {
    Optional<EmailVerificationCodeEntity> selectByEmail(@Param("email") String email);

    int upsert(EmailVerificationCodeEntity record);

    int updateByEmail(EmailVerificationCodeEntity record);

    void updateAttemptCountByEmail(@Param("email") String email, @Param("attemptCount") Integer attemptCount);

    void deleteByEmail(@Param("email") String email);
}
