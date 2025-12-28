package com.yoru.qingxintutor.controller;

import com.yoru.qingxintutor.annotation.AntiReplay;
import com.yoru.qingxintutor.annotation.auth.RequireStudent;
import com.yoru.qingxintutor.filter.CustomUserDetails;
import com.yoru.qingxintutor.pojo.ApiResult;
import com.yoru.qingxintutor.pojo.request.RechargeRequest;
import com.yoru.qingxintutor.pojo.entity.WalletRechargeOrderEntity;
import com.yoru.qingxintutor.pojo.result.RechargeInfoResult;
import com.yoru.qingxintutor.pojo.result.WalletInfoResult;
import com.yoru.qingxintutor.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Controller
@RequestMapping("/api/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    /*
    GET	/	用户	查询本人钱包信息（余额 + 积分）
     */
    @RequireStudent
    @ResponseBody
    @GetMapping
    public ApiResult<WalletInfoResult> getWallet(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(walletService.getWalletInfo(userDetails.getUser().getId()));
    }

    // GET	/recharges	用户	查看充值订单
    @RequireStudent
    @ResponseBody
    @GetMapping("/recharges")
    public ApiResult<List<WalletRechargeOrderEntity>> getRechargeOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.success(walletService.getRechargeOrders(userDetails.getUser().getId()));
    }

    // GET	/recharge/{id}	用户	查看充值订单
    @RequireStudent
    @ResponseBody
    @GetMapping("/recharge/{id}")
    public ApiResult<WalletRechargeOrderEntity> getRechargeOrderById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     @PathVariable
                                                                     @Min(value = 1, message = "Id must be a positive number")
                                                                     Long id
    ) {
        return ApiResult.success(walletService.getRechargeOrderById(userDetails.getUser().getId(), id));
    }

    // POST	/recharge	用户	发起充值请求（生成充值订单，跳转第三方h5支付）
    @AntiReplay
    @RequireStudent
    @ResponseBody
    @PostMapping("/recharge")
    public ApiResult<RechargeInfoResult> recharge(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @Valid @RequestBody RechargeRequest rechargeRequest) {
        // 保存 PENDING 订单 并 返回模拟支付页 URL
        RechargeInfoResult result = walletService.createRechargeOrder(userDetails.getUser().getId(), rechargeRequest);
        return ApiResult.success(result);
    }


    // POST	/callback	系统/支付网关	支付回调接口（验证签名后入账）
    @ResponseBody
    @PostMapping("/callback")
    public String handleCallback(HttpServletRequest request) {
        /*  验证微信/支付宝签名
         if (!verifyRealSignature(request)) return "fail";
        */

        String outTradeNo = request.getParameter("out_trade_no");
        String tradeStatus = request.getParameter("trade_status");
        String totalAmount = request.getParameter("total_amount");

        return walletService.handleRechargeOrder(outTradeNo, tradeStatus);
    }

    // mock-pay页面（模拟支付宝端）
    @GetMapping("/mock-pay")
    public String mockPay(@RequestParam String outTradeNo, Model model) {
        model.addAttribute("outTradeNo", outTradeNo);
        return "pay";
    }
}
