package com.yoru.qingxintutor.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String passwdHash;      // VARCHAR(255) NOT NULL (bcrypt 哈希)
    @JsonIgnore
    private LocalDateTime createTime;
    @JsonIgnore
    private LocalDateTime updateTime;
}
