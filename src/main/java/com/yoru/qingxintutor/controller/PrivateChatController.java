package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.MessageCreateRequest;
import com.yoru.qingxintutor.pojo.result.PrivateChatInfoResult;
import com.yoru.qingxintutor.pojo.result.PrivateMessageInfoResult;
import com.yoru.qingxintutor.service.PrivateChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/private-chat")
public class PrivateChatController {
    @Autowired
    private PrivateChatService privateChatService;
    
    /*
    GET     /messages               -- 老师或学生 查找用户发送的所有消息
    GET     /message/{id}           -- 老师或学生 根据 ID 获取某条私聊消息（非公开）
    GET     /{chatId}/messages      -- 老师或学生 获取指定对话的所有消息
    GET     /all                    -- 老师或学生 获取所有对话列表
    GET     /{chatId}               -- 老师或学生 根据 ID 获取对胡详情
    POST    /{teacherId}/create     -- 学生 发起对话
     */

    @GetMapping("/messages")
    public ApiResult<List<PrivateMessageInfoResult>> getMessages(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(privateChatService.listMessagesByUserId(userDetails.getUser().getId()));
    }

    @GetMapping("/message/{id}")
    public ApiResult<PrivateMessageInfoResult> getMessage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @PathVariable
                                                          @Min(value = 1, message = "Id must be a positive number")
                                                          Long id) {
        return ApiResult.success(privateChatService.findMessageById(userDetails.getUser().getId(), id));
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @PostMapping("/{chatId}/message")
    public ApiResult<PrivateMessageInfoResult> sendMessage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable
                                                           @Min(value = 1, message = "ChatId must be a positive number")
                                                           Long chatId,
                                                           @Valid @RequestBody MessageCreateRequest messageCreateRequest) {
        throw new BusinessException("The method is no longer supported");
    }

    @GetMapping("/{chatId}/messages")
    public ApiResult<List<PrivateMessageInfoResult>> getMessages(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                 @PathVariable
                                                                 @Min(value = 1, message = "ChatId must be a positive number")
                                                                 Long chatId) {
        return ApiResult.success(privateChatService.listMessagesByChatId(userDetails.getUser().getId(), chatId));
    }

    @GetMapping("/all")
    public ApiResult<List<PrivateChatInfoResult>> getChats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(privateChatService.listAll(userDetails.getUser().getId()));
    }

    @GetMapping("/{chatId}")
    public ApiResult<PrivateChatInfoResult> getChatById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable
                                                        @Min(value = 1, message = "ChatId must be a positive number")
                                                        Long chatId) {
        return ApiResult.success(privateChatService.findById(userDetails.getUser().getId(), chatId));
    }

    @RequireStudent
    @PostMapping("/{teacherId}/create")
    public ApiResult<PrivateChatInfoResult> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @PathVariable
                                                   @Min(value = 1, message = "TeacherId must be a positive number")
                                                   Long teacherId) {
        return ApiResult.success(privateChatService.getOrCreateChat(userDetails.getUser().getId(), teacherId));
    }
}
