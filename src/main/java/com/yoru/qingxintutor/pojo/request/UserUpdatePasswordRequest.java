package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import com.yoru.qingxintutor.annotation.StrongPassword;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"newPassword"})
@EqualsAndHashCode(exclude = {"newPassword"})
public class UserUpdatePasswordRequest {
    @OptionalNotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @StrongPassword(message = "Passwords must contain both uppercase and lowercase letters, numbers, and special characters")
    private String newPassword;
}
