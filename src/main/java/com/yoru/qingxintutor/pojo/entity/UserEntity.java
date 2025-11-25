package com.yoru.qingxintutor.pojo.entity;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "passwdHash")
@EqualsAndHashCode(exclude = "passwdHash")
public class UserEntity {
    private String id;              // CHAR(36)
    private String username;        // VARCHAR(50) NOT NULL UNIQUE
    private String nickname;        // VARCHAR(50)
    private String email;           // VARCHAR(100) NOT NULL UNIQUE
    private String icon;            // VARCHAR(255)
    private String address;         // VARCHAR(255)
    private String passwdHash;      // VARCHAR(255) NOT NULL (bcrypt 哈希)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
