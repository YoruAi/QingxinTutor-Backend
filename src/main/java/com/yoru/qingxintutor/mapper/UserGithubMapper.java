package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.UserGithubEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserGithubMapper {
    int insert(UserGithubEntity record);

    Optional<UserGithubEntity> selectBySub(String sub);

    Optional<UserGithubEntity> selectByUserId(String userId);

    int deleteByUserId(String userId);
}
