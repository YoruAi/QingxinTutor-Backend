package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.AppFeedbackMapper;
import com.yoru.qingxintutor.pojo.request.FeedbackCreateRequest;
import com.yoru.qingxintutor.pojo.entity.AppFeedbackEntity;
import com.yoru.qingxintutor.pojo.result.FeedbackInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {
    @Autowired
    private AppFeedbackMapper feedbackMapper;

    public List<FeedbackInfoResult> listAllByUserId(String userId) {
        return feedbackMapper.findByUserId(userId)
                .stream()
                .map(FeedbackService::entityToResult)
                .toList();
    }

    public FeedbackInfoResult findById(String userId, Long id) {
        AppFeedbackEntity feedback = feedbackMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Feedback not found"));
        if (!userId.equals(feedback.getUserId()))
            throw new BusinessException("Feedback not found");
        return entityToResult(feedback);
    }

    public FeedbackInfoResult create(String userId, FeedbackCreateRequest request) {
        AppFeedbackEntity appFeedback = AppFeedbackEntity.builder()
                .userId(userId)
                .title(request.getTitle())
                .content(request.getContent())
                .createTime(LocalDateTime.now())
                .build();
        feedbackMapper.insert(appFeedback);
        return entityToResult(appFeedback);
    }

    private static FeedbackInfoResult entityToResult(AppFeedbackEntity entity) {
        return FeedbackInfoResult.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .build();
    }
}
