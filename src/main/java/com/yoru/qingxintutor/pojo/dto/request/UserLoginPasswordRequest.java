package com.yoru.qingxintutor.pojo.dto.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import com.yoru.qingxintutor.annotation.Username;
import com.yoru.qingxintutor.annotation.ValidTimestamp;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class UserLoginPasswordRequest {
    @OptionalNotBlank(message = "Username cannot be composed of only whitespace characters")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    @Username
    private String username;

    @OptionalNotBlank(message = "Email cannot be composed of only whitespace characters")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be composed of only whitespace characters")
    private String password;

    // 防重放
    @NotNull(message = "Timestamp is required")
    @ValidTimestamp
    private Instant timestamp;

    // 校验
    // - username + password
    // - email + password
    @AssertTrue(message = "Invalid password login argument: please provide [username + password] or [email + password]")
    public boolean isLoginMethodValid() {
        boolean hasUsername = StringUtils.hasText(username);
        boolean hasEmail = StringUtils.hasText(email);

        if (hasUsername && !hasEmail)
            return true;
        if (hasEmail && !hasUsername)
            return true;

        return false;
    }
}
