package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.StudyPlanCreateRequest;
import com.yoru.qingxintutor.pojo.dto.request.StudyPlanUpdateRequest;
import com.yoru.qingxintutor.pojo.result.StudyPlanInfoResult;
import com.yoru.qingxintutor.service.StudyPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-plan")
public class StudyPlanController {

    @Autowired
    private StudyPlanService studyPlanService;

    /*
    POST    /      -- create(return)
    PUT     /:id   -- update
    DELETE  /:id   -- delete/completed
     */
    @RequireStudent
    @PostMapping
    public ApiResult<StudyPlanInfoResult> createStudyPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StudyPlanCreateRequest studyPlanCreateRequest) {
        StudyPlanInfoResult result = studyPlanService.create(userDetails.getUser().getId(), studyPlanCreateRequest);
        return ApiResult.success(result);
    }

    @RequireStudent
    @PutMapping("/{id}")
    public ApiResult<StudyPlanInfoResult> updateStudyPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable
            @Min(value = 1, message = "Id must be a positive number")
            Long id,
            @Valid @RequestBody StudyPlanUpdateRequest studyPlanUpdateRequest) {
        StudyPlanInfoResult result = studyPlanService.update(userDetails.getUser().getId(), id, studyPlanUpdateRequest);
        return ApiResult.success(result);
    }

    @RequireStudent
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteStudyPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable
            @Min(value = 1, message = "Id must be a positive number")
            Long id) {
        studyPlanService.deleteById(userDetails.getUser().getId(), id);
        return ApiResult.success();
    }
}
