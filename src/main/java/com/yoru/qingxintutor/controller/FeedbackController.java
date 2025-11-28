package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.FeedbackCreateRequest;
import com.yoru.qingxintutor.pojo.result.FeedbackInfoResult;
import com.yoru.qingxintutor.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /*
    POST    /   -- create
     */
    @PostMapping
    public ApiResult<FeedbackInfoResult> createFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FeedbackCreateRequest request) {
        FeedbackInfoResult result = feedbackService.create(userDetails.getUser().getId(), request);
        return ApiResult.success(result);
    }
}
