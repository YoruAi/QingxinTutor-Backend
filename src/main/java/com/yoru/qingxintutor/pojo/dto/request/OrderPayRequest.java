package com.yoru.qingxintutor.pojo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPayRequest {
    /**
     * 学生选择使用的奖学券 ID 列表（可为空，表示不使用）
     */
    @Size(max = 10, message = "Can only use 10 voucher for one order")
    private List<Long> voucherIds;
}