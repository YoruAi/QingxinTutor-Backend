package com.yoru.qingxintutor.pojo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"code"})
@EqualsAndHashCode(exclude = {"code"})
public class UserLoginEmailRequest {
    @NotBlank(message = "Email cannot be composed of only whitespace characters")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Code cannot be composed of only whitespace characters")
    @Size(min = 6, max = 6, message = "Code must be 6 characters")
    private String code;
}
