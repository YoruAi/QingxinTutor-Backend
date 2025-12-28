package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import com.yoru.qingxintutor.annotation.StrongPassword;
import com.yoru.qingxintutor.annotation.Username;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "code"})
@EqualsAndHashCode(exclude = {"password", "code"})
public class UserRegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be shorter than 50 characters")
    @Username
    private String username;

    @NotBlank(message = "Email is required")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @OptionalNotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @StrongPassword(message = "Passwords must contain both uppercase and lowercase letters, numbers, and special characters")
    private String password;

    @NotBlank(message = "Code is required")
    @Size(min = 6, max = 6, message = "Code must be 6 characters")
    private String code;

    // 禁止用户注册系统自动注册用户名前缀
    @AssertTrue(message = "The username cannot be registered")
    public boolean isUsernameValid() {
        if (username == null)
            return true;
        return !username.startsWith("github_") && !username.startsWith("email_");
    }
}
