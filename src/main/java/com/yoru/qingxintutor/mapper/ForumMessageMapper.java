package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.ForumMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ForumMessageMapper {
    Optional<ForumMessageEntity> findById(@Param("id") Long id);

    List<ForumMessageEntity> findByForumId(@Param("forumId") Long forumId);

    List<ForumMessageEntity> findByUserId(@Param("userId") String userId);

    int insert(ForumMessageEntity forumMessage);

    List<ForumMessageEntity> findByWeeklyForumId(@Param("forumId") Long forumId);
}
