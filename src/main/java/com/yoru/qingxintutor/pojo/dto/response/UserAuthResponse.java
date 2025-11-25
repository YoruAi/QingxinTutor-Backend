package com.yoru.qingxintutor.pojo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthResponse {
    private String token;
    private final String tokenType = "Bearer";
    private Long expireIn;  // ms
    private AuthedUser user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthedUser {
        private String id;
        private String username;
    }
}