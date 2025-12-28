package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequest {
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @OptionalNotBlank(message = "Title cannot be composed of only whitespace characters")
    @Size(max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @OptionalNotBlank(message = "Content cannot be composed of only whitespace characters")
    @Size(max = 255, message = "Content must be between 1 and 255 characters")
    private String content;
}
