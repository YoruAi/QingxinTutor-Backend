package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateRequest;
import com.yoru.qingxintutor.pojo.result.NotificationInfoResult;
import com.yoru.qingxintutor.pojo.result.UserInfoResult;
import com.yoru.qingxintutor.service.AvatarService;
import com.yoru.qingxintutor.service.NotificationService;
import com.yoru.qingxintutor.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;

    // userinfo //
    @GetMapping("/me")
    public ApiResult<UserInfoResult> getProfiles(@AuthenticationPrincipal CustomUserDetails userDetails)
            throws BusinessException {
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    @PutMapping("/me")
    public ApiResult<UserInfoResult> updateProfiles(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @Valid @RequestBody UserUpdateRequest userUpdateRequest)
            throws BusinessException {
        userService.updateInfo(userDetails.getUser().getId(), userUpdateRequest);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }

    // upload avatar
    @PostMapping("/upload-avatar")
    public ApiResult<UserInfoResult> uploadAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam("file") MultipartFile file)
            throws BusinessException {
        String accessURL = avatarService.uploadAvatar(file);
        userService.updateAvatar(userDetails.getUser().getId(), accessURL);
        return ApiResult.success(userService.getInfo(userDetails.getUser().getId()));
    }


    // feedback, forum_message, review, study_plan, notification //
    /*
    GET    /api/user/feedbacks
    GET    /api/user/feedback/:id
     */
    
    /*
    GET    /api/user/forum-messages
    GET    /api/user/forum-message/:id
    */

    /*
    GET     /api/user/reviews
    GET     /api/user/review/:id
    */
    
    /*
    GET    /api/user/study_plans?subjectName=数学
    GET    /api/user/study_plan/:id
     */

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
                                            Long id)
            throws BusinessException {
        return ApiResult.success(notificationService.findById(userDetails.getUser().getId(), id));
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
