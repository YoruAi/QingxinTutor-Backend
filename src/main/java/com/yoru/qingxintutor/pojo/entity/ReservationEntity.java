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
public class ReservationEntity {
    private Long id;                // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String userId;          // CHAR(36) NOT NULL, REFERENCES user(id)
    private Long teacherId;         // BIGINT NOT NULL, REFERENCES teacher(id)
    private Long subjectId;         // BIGINT NOT NULL, REFERENCES subject(id)
    private LocalDateTime startTime; // DATETIME NOT NULL
    private Integer duration;       // INT NOT NULL, 单位：分钟
    private State state;           // enum NOT NULL
    private LocalDateTime createTime;

    public enum State {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }
}
