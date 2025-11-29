package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.UserWalletMapper;
import com.yoru.qingxintutor.mapper.WalletRechargeOrderMapper;
import com.yoru.qingxintutor.pojo.dto.request.RechargeRequest;
import com.yoru.qingxintutor.pojo.entity.UserWalletEntity;
import com.yoru.qingxintutor.pojo.entity.WalletRechargeOrderEntity;
import com.yoru.qingxintutor.pojo.result.RechargeInfoResult;
import com.yoru.qingxintutor.pojo.result.WalletInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    @Autowired
    private UserWalletMapper walletMapper;

    @Autowired
    private WalletRechargeOrderMapper rechargeOrderMapper;

    public WalletInfoResult getWalletInfo(String userId) {
        return entityToResult(walletMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Wallet not found")));
    }

    public WalletInfoResult create(String userId) {
        LocalDateTime now = LocalDateTime.now();
        UserWalletEntity wallet = UserWalletEntity.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .points(0)
                .createTime(now)
                .updateTime(now)
                .build();
        walletMapper.insert(wallet);
        return entityToResult(wallet);
    }

    public void addBalance(String userId, BigDecimal amount) {
        walletMapper.adjustBalance(userId, amount);
    }

    public void deductBalance(String userId, BigDecimal amount) {
        int updated = walletMapper.adjustBalance(userId, amount.negate());
        if (updated == 0) {
            throw new BusinessException("The balance is insufficient and cannot be deducted");
        }
    }

    public void addPoints(String userId, Integer points) {
        walletMapper.adjustPoints(userId, points);
    }

    public void deductPoints(String userId, Integer points) {
        int updated = walletMapper.adjustPoints(userId, -points);
        if (updated == 0) {
            throw new BusinessException("The points are insufficient and cannot be deducted");
        }
    }

    private static WalletInfoResult entityToResult(UserWalletEntity entity) {
        return WalletInfoResult.builder()
                .balance(entity.getBalance())
                .points(entity.getPoints())
                .build();
    }

    public List<WalletRechargeOrderEntity> getRechargeOrders(String userId) {
        return rechargeOrderMapper.findByUserId(userId);
    }

    public WalletRechargeOrderEntity getRechargeOrderById(String userId, Long id) {
        WalletRechargeOrderEntity rechargeOrder = rechargeOrderMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Recharge order not found"));
        if (!userId.equals(rechargeOrder.getUserId()))
            throw new BusinessException("Recharge order not found");
        return rechargeOrder;
    }

    @Transactional
    public RechargeInfoResult createRechargeOrder(String userId, RechargeRequest rechargeRequest) {
        String outTradeNo = "RECHARGE_" + System.currentTimeMillis() + "_" + userId;
        BigDecimal amount = rechargeRequest.getAmount();
        WalletRechargeOrderEntity.State state = WalletRechargeOrderEntity.State.PENDING;

        // 保存 PENDING 订单
        WalletRechargeOrderEntity rechargeOrder = WalletRechargeOrderEntity.builder()
                .userId(userId)
                .outTradeNo(outTradeNo)
                .amount(amount)
                .state(state)
                .createTime(LocalDateTime.now())
                .build();
        rechargeOrderMapper.insert(rechargeOrder);

        // 返回模拟支付页 URL
        String mockPayUrl = "/api/wallet/mock-pay?outTradeNo=" + outTradeNo;

        return RechargeInfoResult.builder()
                .state(state)
                .payUrl(mockPayUrl)
                .build();
    }

    @Transactional
    public String handleRechargeOrder(String outTradeNo, String tradeStatus) {
        // 幂等处理：查本地订单
        WalletRechargeOrderEntity order = rechargeOrderMapper.findByOutTradeNo(outTradeNo)
                .orElse(null);
        if (order == null)
            return "fail";
        if (order.getState() != WalletRechargeOrderEntity.State.PENDING)
            return "success";

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            addBalance(order.getUserId(), order.getAmount());
            addPoints(order.getUserId(), order.getAmount().intValue() / 10);
            rechargeOrderMapper.markAsSuccess(outTradeNo);
        } else {
            rechargeOrderMapper.markAsFailed(outTradeNo);
        }

        return "success";
    }
}
