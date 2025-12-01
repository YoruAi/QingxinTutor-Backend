package com.yoru.qingxintutor.controller;

import com.github.pagehelper.PageInfo;
import com.yoru.qingxintutor.annotation.auth.RequireTeacher;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.TeacherSearchRequest;
import com.yoru.qingxintutor.pojo.dto.request.TeacherUpdateRequest;
import com.yoru.qingxintutor.pojo.result.TeacherInfoResult;
import com.yoru.qingxintutor.service.AvatarService;
import com.yoru.qingxintutor.service.TeacherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private AvatarService avatarService;

    @GetMapping("/excellent")
    public ApiResult<PageInfo<TeacherInfoResult>> listExcellentTeachers(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                        @RequestParam(defaultValue = "6") Integer pageSize) {
        return ApiResult.success(teacherService.listExcellent(pageNum, pageSize));
    }

    @GetMapping("/all")
    public ApiResult<PageInfo<TeacherInfoResult>> listAllTeachers(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                  @RequestParam(defaultValue = "6") Integer pageSize) {
        return ApiResult.success(teacherService.listAll(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<TeacherInfoResult> getTeacher(@PathVariable("id")
                                                   @Min(value = 1, message = "Id must be a positive number")
                                                   Long id) {
        return ApiResult.success(teacherService.getInfoById(id));
    }

    @GetMapping("/filter")
    public ApiResult<PageInfo<TeacherInfoResult>> listTeachersByArgument(
            @Valid @ModelAttribute TeacherSearchRequest teacherSearchRequest,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "6") Integer pageSize
    ) {
        return ApiResult.success(teacherService.filter(teacherSearchRequest, pageNum, pageSize));
    }

    @GetMapping("/search")
    public ApiResult<PageInfo<TeacherInfoResult>> search(
            @Size(min = 1, message = "Text must be longer than 1 character") String text,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "6") Integer pageSize
    ) {
        return ApiResult.success(teacherService.search(text, pageNum, pageSize));
    }


    // Teacher Info
    @RequireTeacher
    @GetMapping("/me")
    public ApiResult<TeacherInfoResult> getProfiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(teacherService.getInfoByUserId(userDetails.getUser().getId()));
    }

    @RequireTeacher
    @PutMapping("/me")
    public ApiResult<TeacherInfoResult> updateProfiles(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @Valid @RequestBody TeacherUpdateRequest teacherUpdateRequest) {
        teacherService.updateInfoByUserId(userDetails.getUser().getId(), teacherUpdateRequest);
        return ApiResult.success(teacherService.getInfoByUserId(userDetails.getUser().getId()));
    }

    @RequireTeacher
    @PostMapping("/upload-avatar")
    public ApiResult<TeacherInfoResult> uploadAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestParam("file") MultipartFile file) {

        String accessURL = avatarService.uploadAvatar(file);
        teacherService.updateAvatarByUserId(userDetails.getUser().getId(), accessURL);
        return ApiResult.success(teacherService.getInfoByUserId(userDetails.getUser().getId()));
    }
}
