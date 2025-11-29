package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.WalletRechargeOrderEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WalletRechargeOrderMapper {
    Optional<WalletRechargeOrderEntity> findById(Long id);

    Optional<WalletRechargeOrderEntity> findByOutTradeNo(String outTradeNo);

    List<WalletRechargeOrderEntity> findByUserId(String userId);

    Optional<WalletRechargeOrderEntity> findPendingOrderByOutTradeNo(String outTradeNo);

    int insert(WalletRechargeOrderEntity order);

    int markAsSuccess(String outTradeNo);

    int markAsFailed(String outTradeNo);
}
