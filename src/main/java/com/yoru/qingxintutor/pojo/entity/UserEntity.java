package com.yoru.qingxintutor.pojo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

// Used also for UserInfoResult
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
    private String icon;            // VARCHAR(255)
    private String address;         // VARCHAR(255)
    @JsonIgnore
    private String passwdHash;      // VARCHAR(255) NOT NULL (bcrypt 哈希)
    private Role role;              // ENUM('STUDENT', 'TEACHER') NOT NULL
    @JsonIgnore
    private LocalDateTime createTime;
    @JsonIgnore
    private LocalDateTime updateTime;

    public enum Role {
        STUDENT,
        TEACHER;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Role fromString(@JsonProperty String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return Role.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role value");
            }
        }
    }
}
