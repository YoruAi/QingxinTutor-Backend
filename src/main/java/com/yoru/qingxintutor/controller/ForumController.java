package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.MessageCreateRequest;
import com.yoru.qingxintutor.pojo.entity.ForumEntity;
import com.yoru.qingxintutor.pojo.result.ForumMessageInfoResult;
import com.yoru.qingxintutor.service.ForumMessageService;
import com.yoru.qingxintutor.service.ForumService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/forum")
public class ForumController {
    @Autowired
    private ForumMessageService forumMessageService;
    @Autowired
    private ForumService forumService;

    /*
    GET     /messages               -- 查找用户发送的所有消息
    GET     /message/{id}           -- 根据 ID 获取某条论坛消息（公开）
    GET     /{forumId}/messages     -- 获取指定论坛的所有消息
    GET     /all                    -- 获取所有论坛列表
    GET     /{forumId}              -- 根据 ID 获取论坛详情
     */
    @GetMapping("/messages")
    public ApiResult<List<ForumMessageInfoResult>> getMessages(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(forumMessageService.listAllByUserId(userDetails.getUser().getId()));
    }

    @GetMapping("/message/{id}")
    public ApiResult<ForumMessageInfoResult> getMessage(@PathVariable
                                                        @Min(value = 1, message = "Id must be a positive number")
                                                        Long id) {
        return ApiResult.success(forumMessageService.findById(id));
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @PostMapping("/{forumId}/message")
    public ApiResult<ForumMessageInfoResult> sendMessage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @PathVariable
                                                         @Min(value = 1, message = "ForumId must be a positive number")
                                                         Long forumId,
                                                         @Valid @RequestBody MessageCreateRequest forumMessageCreateRequest) {
        throw new BusinessException("The method is no longer supported");
    }

    @GetMapping("/{forumId}/messages")
    public ApiResult<List<ForumMessageInfoResult>> getMessages(@PathVariable
                                                               @Min(value = 1, message = "ForumId must be a positive number")
                                                               Long forumId) {
        return ApiResult.success(forumMessageService.listAllByForumId(forumId));
    }

    @GetMapping("/all")
    public ApiResult<List<ForumEntity>> getForums() {
        return ApiResult.success(forumService.listAll());
    }

    @GetMapping("/{forumId}")
    public ApiResult<ForumEntity> getForumById(@PathVariable
                                               @Min(value = 1, message = "ForumId must be a positive number")
                                               Long forumId) {
        return ApiResult.success(forumService.findById(forumId));
    }
}
