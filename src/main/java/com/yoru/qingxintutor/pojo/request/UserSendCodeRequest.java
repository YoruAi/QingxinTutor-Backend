package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.enums.EmailPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSendCodeRequest {
    @NotBlank(message = "Email is required")
    @Size(min = 1, max = 100, message = "Email must be between 1 and 100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Purpose is required")
    private EmailPurpose purpose;
}
