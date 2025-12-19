package com.yoru.qingxintutor.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStudyPlanEntity {
    private Long id;                // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String userId;          // CHAR(36) NOT NULL, REFERENCES user(id)
    private Long subjectId;         // BIGINT NOT NULL, REFERENCES subject(id)
    private String title;           // VARCHAR(100) NOT NULL
    private String content;         // TEXT
    private LocalDateTime targetCompletionTime; // DATETIME
    private LocalDateTime reminderTime;         // DATETIME
    private Boolean completed;
    @JsonIgnore
    private LocalDateTime createTime;
}
