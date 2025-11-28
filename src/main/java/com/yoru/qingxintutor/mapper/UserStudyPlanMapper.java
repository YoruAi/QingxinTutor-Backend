package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.UserStudyPlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserStudyPlanMapper {
    /**
     * 根据 ID 查询学习计划
     */
    Optional<UserStudyPlanEntity> findById(Long id);

    /**
     * 根据用户 ID 查询所有学习计划（按创建时间倒序）
     */
    List<UserStudyPlanEntity> findByUserId(String userId);

    /**
     * 根据用户 ID 和科目 ID 查询学习计划（按创建时间倒序）
     */
    List<UserStudyPlanEntity> findByUserIdAndSubjectId(@Param("userId") String userId, @Param("subjectId") Long subjectId);

    /**
     * 插入新的学习计划，主键自增
     */
    void insert(UserStudyPlanEntity entity);

    /**
     * 更新学习计划（使用动态 SQL，仅更新非空字段）
     */
    void update(UserStudyPlanEntity entity);

    /**
     * 根据 ID 删除学习计划
     */
    void deleteById(Long id);

    /**
     * 判断用户是否已存在相同标题的学习计划
     */
    boolean existsByUserIdAndTitle(@Param("userId") String userId, @Param("title") String title);

    /**
     * 根据用户ID和标题查找学习计划ID
     */
    Optional<Long> findByUserIdAndTitle(@Param("userId") String userId, @Param("title") String title);
}
