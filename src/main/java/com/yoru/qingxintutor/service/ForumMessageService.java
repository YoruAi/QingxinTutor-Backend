package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.ForumMapper;
import com.yoru.qingxintutor.mapper.ForumMessageMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.ForumMessageCreateRequest;
import com.yoru.qingxintutor.pojo.entity.ForumMessageEntity;
import com.yoru.qingxintutor.pojo.result.ForumMessageInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumMessageService {

    @Autowired
    private ForumMessageMapper forumMessageMapper;

    @Autowired
    private ForumMapper forumMapper;

    @Autowired
    private UserMapper userMapper;

    public List<ForumMessageInfoResult> listAllByUserId(String userId) {
        return forumMessageMapper.findByUserId(userId)
                .stream()
                .map(forumMessage -> entityToResult(forumMessage,
                        forumMapper.findById(forumMessage.getForumId())
                                .orElseThrow(() -> new BusinessException("Forum not found"))
                                .getName(),
                        userMapper.findById(userId)
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername()))
                .toList();
    }

    public List<ForumMessageInfoResult> listAllByForumId(Long forumId) {
        return forumMessageMapper.findByForumId(forumId)
                .stream()
                .map(forumMessage -> entityToResult(forumMessage,
                        forumMapper.findById(forumId)
                                .orElseThrow(() -> new BusinessException("Forum not found"))
                                .getName(),
                        userMapper.findById(forumMessage.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername()))
                .toList();
    }

    public ForumMessageInfoResult findById(Long id) {
        ForumMessageEntity forumMessage = forumMessageMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Forum message not found"));
        String forumName = forumMapper.findById(forumMessage.getForumId())
                .orElseThrow(() -> new BusinessException("Forum not found"))
                .getName();
        String username = userMapper.findById(forumMessage.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        return entityToResult(forumMessage, forumName, username);
    }

    public ForumMessageInfoResult insert(String userId, Long forumId, ForumMessageCreateRequest request) {
        String forumName = forumMapper.findById(forumId)
                .orElseThrow(() -> new BusinessException("Forum not found"))
                .getName();
        String username = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        ForumMessageEntity forumMessage = ForumMessageEntity.builder()
                .forumId(forumId)
                .userId(userId)
                .content(request.getContent())
                .createTime(LocalDateTime.now())
                .build();
        forumMessageMapper.insert(forumMessage);

        return entityToResult(forumMessage, forumName, username);
    }

    public List<ForumMessageInfoResult> listWeeklyByForumId(Long forumId) {
        return forumMessageMapper.findByWeeklyForumId(forumId)
                .stream()
                .map(forumMessage -> entityToResult(forumMessage,
                        forumMapper.findById(forumId)
                                .orElseThrow(() -> new BusinessException("Forum not found"))
                                .getName(),
                        userMapper.findById(forumMessage.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername()))
                .toList();
    }


    private static ForumMessageInfoResult entityToResult(ForumMessageEntity entity, String forumName, String username) {
        return ForumMessageInfoResult.builder()
                .id(entity.getId())
                .forumId(entity.getForumId())
                .forumName(forumName)
                .userId(entity.getUserId())
                .username(username)
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .build();
    }
}
