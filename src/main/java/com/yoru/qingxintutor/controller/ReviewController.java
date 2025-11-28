package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.ReviewCreateRequest;
import com.yoru.qingxintutor.pojo.dto.request.ReviewUpdateRequest;
import com.yoru.qingxintutor.pojo.result.ReviewInfoResult;
import com.yoru.qingxintutor.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /*
    @RequireStudent
    POST    /      -- create(return)
    PUT     /:id   -- update
    DELETE  /:id   -- delete/completed
     */
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
}
