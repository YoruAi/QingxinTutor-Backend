package com.yoru.qingxintutor.pojo.dto.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import com.yoru.qingxintutor.annotation.Username;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.util.StringUtils;

@Deprecated(since = "2.0.0", forRemoval = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "code"})
@EqualsAndHashCode(exclude = {"password", "code"})
public class UserLoginRequest {
    @OptionalNotBlank(message = "Username cannot be composed of only whitespace characters")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    @Username
    private String username;

    @OptionalNotBlank(message = "Email cannot be composed of only whitespace characters")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @OptionalNotBlank(message = "Password cannot be composed of only whitespace characters")
    private String password;

    @OptionalNotBlank(message = "Code cannot be composed of only whitespace characters")
    @Size(min = 6, max = 6, message = "Code must be 6 characters")
    private String code;

    // 校验
    // - username + password
    // - email + password
    // - email + code
    @AssertTrue(message = "Invalid login argument: " +
            "please provide [username + password], [email + password], or [email + code]")
    public boolean isLoginMethodValid() {
        boolean hasUsername = StringUtils.hasText(username);
        boolean hasEmail = StringUtils.hasText(email);
        boolean hasPassword = StringUtils.hasText(password);
        boolean hasCode = StringUtils.hasText(code);

        if (hasUsername && hasPassword && !hasEmail && !hasCode)
            return true;
        if (hasEmail && hasPassword && !hasUsername && !hasCode)
            return true;
        if (hasEmail && hasCode && !hasUsername && !hasPassword)
            return true;

        return false;
    }
}
