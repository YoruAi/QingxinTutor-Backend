package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.NotificationMapper;
import com.yoru.qingxintutor.pojo.entity.NotificationEntity;
import com.yoru.qingxintutor.pojo.result.NotificationInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    public void createGlobalNotification(String title, String content) {
        NotificationEntity notification = NotificationEntity.builder()
                .userId(null)
                .title(title)
                .content(content)
                .build();
        notificationMapper.insert(notification);
    }

    public void createPersonalNotification(String userId, String title, String content) {
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .build();
        notificationMapper.insert(notification);
    }

    public List<NotificationInfoResult> listGlobalNotifications() {
        return notificationMapper.findGlobalNotifications().stream()
                .map(NotificationService::entityToResult)
                .toList();
    }

    public List<NotificationInfoResult> listPersonalNotifications(String userId) {
        return notificationMapper.findByUserId(userId).stream()
                .map(NotificationService::entityToResult)
                .toList();
    }

    public NotificationInfoResult findById(String userId, Long id) throws BusinessException {
        NotificationEntity notification = notificationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Notification not found"));
        if (notification.getUserId() != null && !userId.equals(notification.getUserId()))
            throw new BusinessException("Notification not found");
        return entityToResult(notification);
    }


    private static NotificationInfoResult entityToResult(NotificationEntity entity) {
        return NotificationInfoResult.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .build();
    }
}
