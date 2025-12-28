package com.yoru.qingxintutor.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"code"})
@EqualsAndHashCode(exclude = {"code"})
public class UserUpdateGithubRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Code_verifier is required")
    private String code_verifier;
}
