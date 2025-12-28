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
public class MessageCreateRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 255, message = "Content must be between 1 and 255 characters")
    private String content;
}
