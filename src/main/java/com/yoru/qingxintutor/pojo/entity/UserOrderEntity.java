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
public class UserOrderEntity {
    private Long id;                // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String userId;          // CHAR(36) NOT NULL, REFERENCES user(id)
    private Long reservationId;     // BIGINT NOT NULL
    private String item;            // VARCHAR(100) NOT NULL
    private Integer quantity;       // INT DEFAULT 1
    private BigDecimal price;       // decimal(10,2) NOT NULL
    private State state;            // enum NOT NULL 
    private LocalDateTime createTime;

    public enum State {
        PENDING,
        PAID,
        CANCELLED
    }
}
