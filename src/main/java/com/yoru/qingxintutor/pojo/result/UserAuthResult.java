package com.yoru.qingxintutor.pojo.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthResult {
    private String token;
    private final String tokenType = "Bearer";
    private Long expireIn;  // ms
    private String userId;
    private String username;
}