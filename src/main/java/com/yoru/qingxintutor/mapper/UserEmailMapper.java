package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.UserEmailEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserEmailMapper {
    int insert(UserEmailEntity record);

    Optional<UserEmailEntity> selectByEmail(String email);

    Optional<UserEmailEntity> selectByUserId(String userId);

    int deleteByUserId(String userId);
}
