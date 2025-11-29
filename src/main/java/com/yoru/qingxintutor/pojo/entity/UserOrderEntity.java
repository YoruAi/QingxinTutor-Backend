package com.yoru.qingxintutor.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderEntity {
    private Long id;                // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String userId;          // CHAR(36) NOT NULL, REFERENCES user(id)
    private Long reservationId;     // BIGINT NOT NULL
    private String item;            // VARCHAR(100) NOT NULL
    private Integer quantity;       // INT DEFAULT 1
    private Long price;             // BIGINT NOT NULL, 单位：分
    private State state;           // VARCHAR(20) NOT NULL 
    private LocalDateTime createTime;

    public enum State {
        PENDING,
        PAID,
        CANCELED
    }
}
