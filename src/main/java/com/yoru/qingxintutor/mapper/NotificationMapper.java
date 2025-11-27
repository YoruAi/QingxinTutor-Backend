package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationMapper {
    Optional<NotificationEntity> findById(Long id);

    List<NotificationEntity> findGlobalNotifications();

    List<NotificationEntity> findByUserId(String userId);

    List<NotificationEntity> findAll();

    void insert(NotificationEntity notification);

    void deleteById(Long id);
}
