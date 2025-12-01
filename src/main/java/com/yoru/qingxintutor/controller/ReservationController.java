package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.annotation.auth.RequireTeacher;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.dto.request.ReservationCreateRequest;
import com.yoru.qingxintutor.pojo.entity.ReservationEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.result.ReservationInfoResult;
import com.yoru.qingxintutor.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    /*
    GET	    /api/reservation	    用户/教师	查询学生发送的预约/教师接收到的预约（可选筛选：状态）
    GET	    /api/reservation/{id}	用户/教师	获取预约详情
    POST	/api/reservation	    用户	        创建新预约(检查教师是否有空)
    PUT	    /api/reservation/{id}/cancel	用户/教师	用户取消预约(仅PENDING) → CANCELLED，级联取消未支付订单，已支付则退款
    PUT	    /api/reservation/{id}/confirm	教师	        教师确认预约(仅全部订单PAID) → CONFIRMED
    PUT	    /api/reservation/{id}/complete	教师	        结课(仅CONFIRMED)→ COMPLETED
     */

    /**
     * 查询预约列表：
     * - 学生：查看自己发起的所有预约
     * - 教师：查看自己收到的所有预约
     */
    @GetMapping
    public ApiResult<List<ReservationInfoResult>> listReservations(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                   @RequestParam(required = false) ReservationEntity.State state) {
        List<ReservationInfoResult> result;
        UserEntity.Role role = userDetails.getUser().getRole();
        if (role == UserEntity.Role.STUDENT) {
            result = reservationService.listStudentReservations(userDetails.getUser().getId(), state);
        } else if (role == UserEntity.Role.TEACHER) {
            result = reservationService.listTeacherReservations(userDetails.getUser().getId(), state);
        } else {
            throw new BusinessException("Unknown role");
        }
        return ApiResult.success(result);
    }

    /**
     * 获取预约详情（学生或教师）
     */
    @GetMapping("/{id}")
    public ApiResult<ReservationInfoResult> getReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") @Min(value = 1, message = "Id must be a positive number") Long id) {
        ReservationInfoResult result;
        UserEntity.Role role = userDetails.getUser().getRole();
        if (role == UserEntity.Role.STUDENT) {
            result = reservationService.getStudentReservation(userDetails.getUser().getId(), id);
        } else if (role == UserEntity.Role.TEACHER) {
            result = reservationService.getTeacherReservation(userDetails.getUser().getId(), id);
        } else {
            throw new BusinessException("Unknown role");
        }
        return ApiResult.success(result);
    }

    /**
     * 用户创建新预约（仅学生）
     */
    @RequireStudent
    @PostMapping
    public ApiResult<ReservationInfoResult> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReservationCreateRequest request) {
        ReservationInfoResult result = reservationService.createReservation(userDetails.getUser().getId(), request);
        return ApiResult.success(result);
    }

    /**
     * 取消预约（学生或教师，仅限 PENDING 状态）
     */
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") @Min(value = 1, message = "Id must be a positive number") Long id) {
        UserEntity.Role role = userDetails.getUser().getRole();
        if (role == UserEntity.Role.STUDENT) {
            reservationService.cancelReservationByStudent(userDetails.getUser().getId(), id);
        } else if (role == UserEntity.Role.TEACHER) {
            reservationService.cancelReservationByTeacher(userDetails.getUser().getId(), id);
        } else {
            throw new BusinessException("Unknown role");
        }
        return ApiResult.success();
    }

    /**
     * 教师确认预约（仅当 PENDING 状态且所有订单已支付）
     */
    @RequireTeacher
    @PutMapping("/{id}/confirm")
    public ApiResult<Void> confirmReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") @Min(value = 1, message = "Id must be a positive number") Long id) {
        reservationService.confirmReservation(userDetails.getUser().getId(), id);
        return ApiResult.success();
    }

    /**
     * 教师结课（仅 CONFIRMED 状态）
     */
    @RequireTeacher
    @PutMapping("/{id}/complete")
    public ApiResult<Void> completeReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") @Min(value = 1, message = "Id must be a positive number") Long id) {
        reservationService.completeReservation(userDetails.getUser().getId(), id);
        return ApiResult.success();
    }
}
