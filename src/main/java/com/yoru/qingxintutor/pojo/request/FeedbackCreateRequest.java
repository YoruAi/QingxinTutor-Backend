package com.yoru.qingxintutor.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 255, message = "Content must be between 1 and 255 characters")
    private String content;
}
