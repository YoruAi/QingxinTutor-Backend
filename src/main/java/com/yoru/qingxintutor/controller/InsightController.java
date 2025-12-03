package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.annotation.auth.RequireTeacher;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/insight")
public class InsightController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * 获取学习建议（面向学生）
     */
    @RequireStudent
    @GetMapping("/recommend-learning")
    public ApiResult<Map<String, String>> learningRecommend(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String result = analyticsService.learningRecommend(userDetails.getUser().getId());
        return ApiResult.success(Map.of("aiResult", result));
    }

    /**
     * 获取课程排期优化建议（面向教师）
     */
    @RequireTeacher
    @GetMapping("/scheduling-advice")
    public ApiResult<Map<String, String>> getSchedulingAdvice(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String advice = analyticsService.generateSchedulingAdvice(userDetails.getUser().getId());
        return ApiResult.success(Map.of("aiResult", advice));
    }

    /**
     * 获取教学反馈摘要（面向教师）
     */
    @RequireTeacher
    @GetMapping("/feedback-summary")
    public ApiResult<Map<String, String>> getTeachingFeedbackSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String summary = analyticsService.generateTeachingFeedbackSummary(userDetails.getUser().getId());
        return ApiResult.success(Map.of("aiResult", summary));
    }
}
