package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.AppFeedbackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AppFeedbackMapper {
    Optional<AppFeedbackEntity> findById(@Param("id") Long id);

    List<AppFeedbackEntity> findByUserId(@Param("userId") String userId);

    // 管理员获取
    List<AppFeedbackEntity> findAll();

    int insert(AppFeedbackEntity appFeedback);
}
