package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.annotation.auth.RequireTeacher;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.OrderCreateRequest;
import com.yoru.qingxintutor.pojo.request.OrderPayRequest;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.entity.UserOrderEntity;
import com.yoru.qingxintutor.pojo.result.OrderInfoResult;
import com.yoru.qingxintutor.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /*
    POST	/api/order/reservation/{reservationId}	教师为某预约创建新订单（仅当 reservation.state = PENDING）
    GET	    /api/order/{id}	用户	查看订单详情
    GET	    /api/orders	用户	查询本人所有订单（可按 reservation_id 过滤）
    PUT	    /api/order/{id}/pay	用户	支付订单（→ PAID，扣钱包余额与选择的奖学券）
    PUT	    /api/order/{id}/cancel	用户/教师	取消某订单（仅 PENDING 状态）→ CANCELLED
    限制：一旦预约进入 CONFIRMED，禁止再创建新订单。
     */

    // 教师或学生 查询用户所有订单（可选按 reservationId 或 state 过滤）
    @GetMapping
    public ApiResult<List<OrderInfoResult>> listOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false)
            @Min(value = 1, message = "ReservationId must be a positive number")
            Long reservationId,
            @RequestParam(required = false)
            UserOrderEntity.State state) {
        List<OrderInfoResult> result;
        UserEntity.Role role = userDetails.getUser().getRole();
        if (role == UserEntity.Role.TEACHER)
            result = orderService.listTeacherOrders(userDetails.getUser().getId(), reservationId, state);
        else if (role == UserEntity.Role.STUDENT)
            result = orderService.listStudentOrders(userDetails.getUser().getId(), reservationId, state);
        else
            throw new BusinessException("Unknown role");
        return ApiResult.success(result);
    }

    // 教师或学生 查看订单详情
    @GetMapping("/{id}")
    public ApiResult<OrderInfoResult> getOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable("id")
                                               @Min(value = 1, message = "Id must be a positive number")
                                               Long id) {
        OrderInfoResult result;
        UserEntity.Role role = userDetails.getUser().getRole();
        if (role == UserEntity.Role.TEACHER)
            result = orderService.getTeacherOrder(userDetails.getUser().getId(), id);
        else if (role == UserEntity.Role.STUDENT)
            result = orderService.getStudentOrder(userDetails.getUser().getId(), id);
        else
            throw new BusinessException("Unknown role");
        return ApiResult.success(result);
    }

    // 教师 为预约创建新订单
    @RequireTeacher
    @PostMapping("/reservation/{reservationId}")
    public ApiResult<OrderInfoResult> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("reservationId")
            @Min(value = 1, message = "ReservationId must be a positive number")
            Long reservationId,
            @RequestBody @Valid OrderCreateRequest request) {
        OrderInfoResult result = orderService.createOrder(userDetails.getUser().getId(), reservationId, request);
        return ApiResult.success(result);
    }

    // 学生 支付订单
    @RequireStudent
    @PutMapping("/{id}/pay")
    public ApiResult<Void> payOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable
                                    @Min(value = 1, message = "Id must be a positive number")
                                    Long id,
                                    @Valid @RequestBody OrderPayRequest orderPayRequest) {
        orderService.payOrder(userDetails.getUser().getId(), id, orderPayRequest);
        return ApiResult.success();
    }

    // 学生/教师 取消订单
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable
                                       @Min(value = 1, message = "Id must be a positive number")
                                       Long id) {
        UserEntity.Role role = userDetails.getUser().getRole();
        if (role == UserEntity.Role.TEACHER)
            orderService.cancelOrderByTeacher(userDetails.getUser().getId(), id);
        else if (role == UserEntity.Role.STUDENT)
            orderService.cancelOrderByStudent(userDetails.getUser().getId(), id);
        else
            throw new BusinessException("Unknown role");
        return ApiResult.success();
    }
}
