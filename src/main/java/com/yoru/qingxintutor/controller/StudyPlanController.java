package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.StudyPlanCreateRequest;
import com.yoru.qingxintutor.pojo.request.StudyPlanUpdateRequest;
import com.yoru.qingxintutor.pojo.result.StudyPlanInfoResult;
import com.yoru.qingxintutor.service.StudyPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/study-plan")
public class StudyPlanController {
    @Autowired
    private StudyPlanService studyPlanService;

    /*
    @RequireStudent
    GET     /all?subjectName=数学?completed=false
    GET     /:id
    POST    /      -- create(return)
    PUT     /:id   -- update
    DELETE  /:id   -- delete/completed
     */
    @RequireStudent
    @GetMapping("/all")
    public ApiResult<List<StudyPlanInfoResult>> getStudyPlans(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "subjectName", required = false) String subjectName,
            @RequestParam(name = "completed", required = false) Boolean completed) {
        if (StringUtils.hasText(subjectName))
            return ApiResult.success(studyPlanService.listAllBySubjectName(userDetails.getUser().getId(), subjectName, completed));
        else
            return ApiResult.success(studyPlanService.listAll(userDetails.getUser().getId(), completed));
    }

    @RequireStudent
    @GetMapping("/{id}")
    public ApiResult<StudyPlanInfoResult> getStudyPlanById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable("id")
                                                           @Min(value = 1, message = "Id must be a positive number")
                                                           Long id) {
        return ApiResult.success(studyPlanService.findById(userDetails.getUser().getId(), id));
    }

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
    @PutMapping("/{id}/complete")
    public ApiResult<StudyPlanInfoResult> completeStudyPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable
            @Min(value = 1, message = "Id must be a positive number")
            Long id) {
        studyPlanService.complete(userDetails.getUser().getId(), id);
        return ApiResult.success();
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
