package com.yoru.qingxintutor.pojo.request;

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
public class UserUpdateEmailRequest {
    @NotBlank(message = "Email is required")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "Code is required")
    @Size(min = 6, max = 6, message = "Code must be 6 characters")
    private String code;
}
