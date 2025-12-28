package com.yoru.qingxintutor.controller;

import com.github.pagehelper.PageInfo;
import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.ReviewCreateRequest;
import com.yoru.qingxintutor.pojo.request.ReviewUpdateRequest;
import com.yoru.qingxintutor.pojo.result.ReviewInfoResult;
import com.yoru.qingxintutor.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    /*
    GET     /:id   -- get (公共)
    @RequireStudent
    GET     /all   -- 学生发送的所有评论
    POST    /      -- create
    PUT     /:id   -- update
    DELETE  /:id   -- delete/completed
    GET     /teacher/{teacherId}    -- 教师所有评论
     */
    @RequireStudent
    @GetMapping("/all")
    public ApiResult<List<ReviewInfoResult>> getReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(reviewService.listAllByUserId(userDetails.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ApiResult<ReviewInfoResult> getReviewById(@PathVariable("id")
                                                     @Min(value = 1, message = "Id must be a positive number")
                                                     Long id) {
        return ApiResult.success(reviewService.findById(id));
    }

    @RequireStudent
    @PostMapping
    public ApiResult<ReviewInfoResult> createReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody ReviewCreateRequest request) {
        ReviewInfoResult result = reviewService.create(userDetails.getUser().getId(), request);
        return ApiResult.success(result);
    }

    @RequireStudent
    @PutMapping("/{id}")
    public ApiResult<ReviewInfoResult> updateReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @PathVariable("id")
                                                    @Min(value = 1, message = "Id must be a positive number")
                                                    Long id,
                                                    @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewInfoResult result = reviewService.update(userDetails.getUser().getId(), id, request);
        return ApiResult.success(result);
    }

    @RequireStudent
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("id")
                                        @Min(value = 1, message = "Id must be a positive number")
                                        Long id) {
        reviewService.deleteById(userDetails.getUser().getId(), id);
        return ApiResult.success();
    }

    @GetMapping("/teacher/{teacherId}")
    public ApiResult<PageInfo<ReviewInfoResult>> getTeacherReviews(@PathVariable("teacherId")
                                                                   @Min(value = 1, message = "TeacherId must be a positive number")
                                                                   Long teacherId,
                                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                                   @RequestParam(defaultValue = "6") Integer pageSize) {
        return ApiResult.success(reviewService.findReviewsByTeacherId(teacherId, pageNum, pageSize));
    }

    @RequireStudent
    @GetMapping("/teacher/{teacherId}/my")
    public ApiResult<ReviewInfoResult> getTeacherReviewByStudent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                 @PathVariable("teacherId")
                                                                 @Min(value = 1, message = "TeacherId must be a positive number")
                                                                 Long teacherId) {
        return ApiResult.success(reviewService.findReviewsByTeacherIdAndStudentId(teacherId, userDetails.getUser().getId()));
    }
}
