package com.yoru.qingxintutor.pojo.dto.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanUpdateRequest {
    @OptionalNotBlank(message = "SubjectName cannot be composed of only whitespace characters")
    @Size(min = 1, max = 50, message = "SubjectName must be between 1 and 50 characters")
    private String subjectName;

    @OptionalNotBlank(message = "Title cannot be composed of only whitespace characters")
    @Size(min = 1, max = 100, message = "SubjectName must be between 1 and 100 characters")
    private String title;

    @OptionalNotBlank(message = "Content cannot be composed of only whitespace characters")
    @Size(min = 1, max = 255, message = "SubjectName must be between 1 and 255 characters")
    private String content;

    @Future(message = "TargetCompletionTime must be in the future")
    private LocalDateTime targetCompletionTime;

    @Future(message = "ReminderTime must be in the future")
    private LocalDateTime reminderTime;
}
