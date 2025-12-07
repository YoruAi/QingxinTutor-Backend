package com.yoru.qingxintutor.pojo.result;

import com.yoru.qingxintutor.pojo.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResult {
    private String id;
    private String username;
    private String nickname;
    private String icon;
    private String address;
    private UserEntity.Role role;
    private AuthInfo auth;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthInfo {
        private boolean hasPassword;
        private String email;
        private String githubId;
    }
}
