package com.yoru.qingxintutor.pojo.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreateRequest {
    @NotNull(message = "TeacherId is required")
    @Min(value = 1, message = "TeacherId must be a positive number")
    private Long teacherId;

    @NotBlank(message = "SubjectName is required")
    @Size(max = 50, message = "SubjectName must be between 1 and 50 characters")
    private String subjectName;

    @NotNull(message = "StartTime is required")
    @Future(message = "StartTime must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be between 30 and 300")
    @Max(value = 300, message = "Duration must be between 30 and 300")
    private Integer duration;
}
