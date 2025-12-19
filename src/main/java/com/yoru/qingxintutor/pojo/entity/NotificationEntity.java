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
public class NotificationEntity {
    private Long id;                // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String userId;          // CHAR(36), REFERENCES user(id); NULL 表示全站通知
    private String title;           // VARCHAR(100) NOT NULL
    private String content;         // TEXT NOT NULL
    private LocalDateTime createTime;
}
