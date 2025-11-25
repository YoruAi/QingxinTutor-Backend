package com.yoru.qingxintutor.pojo.entity;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "code")
@EqualsAndHashCode(exclude = "code")
public class EmailVerificationCodeEntity {
    private Long id;
    private String email;
    private String code;
    private Integer attemptCount;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}