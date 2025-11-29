package com.yoru.qingxintutor.pojo.entity;

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
public class UserWalletEntity {
    private Long id;                // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String userId;          // CHAR(36) NOT NULL UNIQUE, REFERENCES user(id)
    private BigDecimal balance;     // BIGINT NOT NULL
    private Integer points;         // INT DEFAULT 0
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}