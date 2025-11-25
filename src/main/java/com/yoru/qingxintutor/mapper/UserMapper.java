package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    /**
     * 根据 ID 查询用户
     */
    Optional<UserEntity> findById(@Param("id") String id);

    /**
     * 根据 ID 查询用户是否存在
     */
    boolean existsById(@Param("id") String id);

    /**
     * 根据用户名查询用户（用于登录）
     */
    Optional<UserEntity> findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    Optional<UserEntity> findByEmail(@Param("email") String email);

    /**
     * 查询所有用户（可加 limit 分页）
     */
    List<UserEntity> findAll();

    /**
     * 插入新用户
     */
    int insert(UserEntity user);

    /**
     * 更新用户信息（不包括密码）
     */
    int update(UserEntity user);

    /**
     * 更新用户密码
     */
    int updatePassword(@Param("id") String id, @Param("passwdHash") String passwdHash);

    /**
     * 根据用户名模糊搜索
     */
    List<UserEntity> searchByUsername(@Param("keyword") String keyword);
}
