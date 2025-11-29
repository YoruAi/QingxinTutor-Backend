package com.yoru.qingxintutor.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletRechargeOrderEntity {
    private Long id;
    private String userId;
    private String outTradeNo;
    private BigDecimal amount;
    private State state;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;

    public enum State {
        PENDING, SUCCESS, FAILED
    }
}