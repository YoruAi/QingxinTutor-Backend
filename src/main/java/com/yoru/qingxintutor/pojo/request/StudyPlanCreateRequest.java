package com.yoru.qingxintutor.pojo.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class StudyPlanCreateRequest {
    @NotBlank(message = "SubjectName is required")
    @Size(max = 50, message = "SubjectName must be between 1 and 50 characters")
    private String subjectName;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 255, message = "Content must be between 1 and 255 characters")
    private String content;

    @NotNull(message = "TargetCompletionTime is required")
    @Future(message = "TargetCompletionTime must be in the future")
    private LocalDateTime targetCompletionTime;

    @NotNull(message = "ReminderTime is required")
    @Future(message = "ReminderTime must be in the future")
    private LocalDateTime reminderTime;
}
