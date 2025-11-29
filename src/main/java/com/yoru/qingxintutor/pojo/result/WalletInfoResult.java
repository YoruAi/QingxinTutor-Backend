package com.yoru.qingxintutor.pojo.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletInfoResult {
    private BigDecimal balance;
    private Integer points;
}
