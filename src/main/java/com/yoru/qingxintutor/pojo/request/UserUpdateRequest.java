package com.yoru.qingxintutor.pojo.request;

import com.yoru.qingxintutor.annotation.OptionalNotBlank;
import com.yoru.qingxintutor.annotation.Username;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @OptionalNotBlank(message = "Username cannot be composed of only whitespace characters")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    @Username
    private String username;

    @OptionalNotBlank(message = "Nickname cannot be composed of only whitespace characters")
    @Size(min = 1, max = 50, message = "Nickname must be between 1 and 50 characters")
    private String nickname;

    @OptionalNotBlank(message = "Address cannot be composed of only whitespace characters")
    @Size(min = 1, max = 255, message = "Address must be between 1 and 255 characters")
    private String address;
}
