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
public class NotificationInfoResult {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createTime;
}
