package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.UserWalletEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Optional;

@Mapper
public interface UserWalletMapper {

    Optional<UserWalletEntity> findByUserId(@Param("userId") String userId);

    int insert(UserWalletEntity wallet);

    int adjustBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    int adjustPoints(@Param("userId") String userId, @Param("points") Integer points);
}
