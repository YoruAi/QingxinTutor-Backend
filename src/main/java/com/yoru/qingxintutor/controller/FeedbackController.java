package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.FeedbackCreateRequest;
import com.yoru.qingxintutor.pojo.result.FeedbackInfoResult;
import com.yoru.qingxintutor.service.FeedbackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    /*
    GET    /all
    GET    /:id
     */
    @GetMapping("/all")
    public ApiResult<List<FeedbackInfoResult>> getFeedbacks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(feedbackService.listAllByUserId(userDetails.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ApiResult<FeedbackInfoResult> getFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable
                                                     @Min(value = 1, message = "Id must be a positive number")
                                                     Long id) {
        return ApiResult.success(feedbackService.findById(userDetails.getUser().getId(), id));
    }

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
