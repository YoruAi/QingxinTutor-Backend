package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"newPassword", "code"})
@EqualsAndHashCode(exclude = {"newPassword", "code"})
public class UserResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @StrongPassword(message = "Passwords must contain both uppercase and lowercase letters, numbers, and special characters")
    private String newPassword;

    @NotBlank(message = "Code is required")
    @Size(min = 6, max = 6, message = "Code must be 6 characters")
    private String code;
}
