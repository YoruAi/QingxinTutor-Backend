package com.yoru.qingxintutor.pojo.result;

import com.yoru.qingxintutor.pojo.entity.UserOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfoResult {
    private Long id;
    private String userId;
    private String username;
    private Long reservationId;
    private String item;
    private Integer quantity;
    private BigDecimal price;
    private UserOrderEntity.State state;
    private LocalDateTime createTime;
}
