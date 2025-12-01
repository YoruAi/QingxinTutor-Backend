package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.entity.UserVoucherEntity;
import com.yoru.qingxintutor.service.VoucherService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    // 奖学券信息获取
    /*
    @RequireStudent
    GET	/api/vouchers	    用户 查询本人所有奖学券
    GET	/api/voucher/:id	用户 查询本人某一奖学券
     */
    @RequireStudent
    @GetMapping("/all")
    public ApiResult<List<UserVoucherEntity>> getAllVouchers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserVoucherEntity> vouchers = voucherService.listAllByUserId(userDetails.getUser().getId());
        return ApiResult.success(vouchers);
    }

    @RequireStudent
    @GetMapping("/{id}")
    public ApiResult<UserVoucherEntity> getVoucherById(
            @PathVariable
            @Min(value = 1, message = "Id must be a positive number")
            Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserVoucherEntity voucher = voucherService.findById(userDetails.getUser().getId(), id);
        return ApiResult.success(voucher);
    }
}
