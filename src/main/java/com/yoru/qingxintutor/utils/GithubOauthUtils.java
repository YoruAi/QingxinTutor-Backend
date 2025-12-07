package com.yoru.qingxintutor.utils;

import com.yoru.qingxintutor.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Component
public class GithubOauthUtils {
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String REDIRECT_URL;

    @Autowired
    private RestTemplate restTemplate;

    public GithubOauthUtils(@Value("${app.oauth.github.client-id}") String clientId,
                            @Value("${app.oauth.github.client-secret}") String clientSecret,
                            @Value("${app.oauth.github.redirect-url}") String redirectUrl) {
        CLIENT_ID = clientId;
        CLIENT_SECRET = clientSecret;
        REDIRECT_URL = redirectUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GithubUserInfo {
        private Integer id;
        private String login;
        private String avatarUrl;
    }

    public GithubUserInfo fetchGithubUserInfo(String code, String code_verifier) {
        // 用 code 换 access_token
        String tokenUrl = "https://github.com/login/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("redirect_uri", REDIRECT_URL);
        body.add("code", code);
        body.add("code_verifier", code_verifier);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);
        //noinspection ConstantValue
        if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
            throw new BusinessException("Failed to get access token");
        }
        String accessToken = (String) tokenResponse.getBody().get("access_token");
        if (accessToken == null) {
            throw new BusinessException("Invalid code");
        }

        // 用 access_token 获取用户信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userRequest,
                Map.class
        );
        //noinspection ConstantValue
        if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
            Map<String, Object> userData = userResponse.getBody();
            Integer githubUserId = (Integer) userData.get("id");
            if (githubUserId == null) {
                throw new BusinessException("Incomplete GitHub user data");
            }
            String githubUsername = "github_" + Objects.requireNonNullElseGet((String) userData.get("login"), githubUserId::toString);
            String avatarUrl = (String) userData.get("avatar_url");

            return GithubUserInfo.builder()
                    .id(githubUserId)
                    .login(githubUsername)
                    .avatarUrl(avatarUrl)
                    .build();
        }

        throw new BusinessException("Failed to fetch user info");
    }
}
