package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateRequest;
import com.yoru.qingxintutor.pojo.result.*;
import com.yoru.qingxintutor.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;

    // userinfo //
    @GetMapping("/me")
    public ApiResult<UserInfoResult> getProfiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    @PutMapping("/me")
    public ApiResult<UserInfoResult> updateProfiles(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        userService.updateInfo(userDetails.getUser().getId(), userUpdateRequest);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    // upload avatar
    @PostMapping("/upload-avatar")
    public ApiResult<UserInfoResult> uploadAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam("file") MultipartFile file) {
        String accessURL = avatarService.uploadAvatar(file);
        userService.updateAvatar(userDetails.getUser().getId(), accessURL);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }


    // feedback, forum_message, review, study_plan, notification //
    /*
    GET    /api/user/feedbacks
    GET    /api/user/feedback/:id
     */
    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/feedbacks")
    public ApiResult<List<FeedbackInfoResult>> getFeedbacks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(feedbackService.listAllByUserId(userDetails.getUser().getId()));
    }

    @GetMapping("/feedback/{id}")
    public ApiResult<FeedbackInfoResult> getFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable
                                                     @Min(value = 1, message = "Id must be a positive number")
                                                     Long id) {
        return ApiResult.success(feedbackService.findById(userDetails.getUser().getId(), id));
    }

    /*
    GET    /api/user/forum-messages
    */
    @Autowired
    private ForumMessageService forumMessageService;

    @GetMapping("/forum-messages")
    public ApiResult<List<ForumMessageInfoResult>> getMessages(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(forumMessageService.listAllByUserId(userDetails.getUser().getId()));
    }

    /*
    @RequireStudent
    GET     /api/user/reviews
    */
    @Autowired
    private ReviewService reviewService;

    @RequireStudent
    @GetMapping("/reviews")
    public ApiResult<List<ReviewInfoResult>> getReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(reviewService.listAllByUserId(userDetails.getUser().getId()));
    }

    /*
    GET    /api/user/notifications/global
    GET    /api/user/notifications/personal
    GET    /api/user/notification/:id
     */
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notifications/global")
    public ApiResult<List<NotificationInfoResult>> getGlobalNotifications() {
        return ApiResult.success(notificationService.listGlobalNotifications());
    }

    @GetMapping("/notifications/personal")
    public ApiResult<List<NotificationInfoResult>> getPersonalNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(notificationService.listPersonalNotifications(userDetails.getUser().getId()));
    }

    @GetMapping("/notification/{id}")
    public ApiResult<?> getNotificationById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable("id")
                                            @Min(value = 1, message = "Id must be a positive number")
                                            Long id) {
        return ApiResult.success(notificationService.findById(userDetails.getUser().getId(), id));
    }

    /*
    @RequireStudent
    GET    /api/user/study_plans?subjectName=数学
    GET    /api/user/study_plan/:id
     */
    @Autowired
    private StudyPlanService studyPlanService;

    @RequireStudent
    @GetMapping("/study-plans")
    public ApiResult<List<StudyPlanInfoResult>> getStudyPlans(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestParam(required = false) String subjectName) {
        if (StringUtils.hasText(subjectName))
            return ApiResult.success(studyPlanService.listAllBySubjectName(userDetails.getUser().getId(), subjectName));
        else
            return ApiResult.success(studyPlanService.listAll(userDetails.getUser().getId()));
    }

    @RequireStudent
    @GetMapping("/study-plan/{id}")
    public ApiResult<StudyPlanInfoResult> getStudyPlanById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable("id")
                                                           @Min(value = 1, message = "Id must be a positive number")
                                                           Long id) {
        return ApiResult.success(studyPlanService.findById(userDetails.getUser().getId(), id));
    }


    // wallet, order, voucher, reservation //
    /*
    GET    /api/user/wallet
    GET    /api/user/orders
    GET    /api/user/vouchers
    GET    /api/user/reservations
    
    GET    /api/user/study-plans
    POST   /api/user/study-plans
    PUT    /api/user/study-plans/{id}  -- update
     */
}
