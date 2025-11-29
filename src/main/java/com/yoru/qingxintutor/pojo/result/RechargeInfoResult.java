package com.yoru.qingxintutor.pojo.result;

import com.yoru.qingxintutor.pojo.entity.WalletRechargeOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeInfoResult {
    private WalletRechargeOrderEntity.State state;
    private String payUrl;
}
