package com.yoru.qingxintutor.pojo.dto.request;

import com.yoru.qingxintutor.annotation.ValidTimestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"code"})
@EqualsAndHashCode(exclude = {"code"})
public class UserLoginGithubRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Code_verifier is required")
    private String code_verifier;

    // 防重放
    @NotNull(message = "Timestamp is required")
    @ValidTimestamp
    private Instant timestamp;
}
