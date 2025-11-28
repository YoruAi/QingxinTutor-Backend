package com.yoru.qingxintutor.pojo.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewInfoResult {
    private Long id;
    private String userId;
    private Long teacherId;
    private String teacherName;
    private Integer rating;
    private String title;
    private String content;
    private LocalDateTime createTime;
}
