package com.yoru.qingxintutor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoru.qingxintutor.filter.JwtAuthFilter;
import com.yoru.qingxintutor.pojo.ApiResult;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/wallet/mock-pay").permitAll()
                        .requestMatchers("/api/wallet/callback").permitAll()
                        .requestMatchers("/file/**").permitAll()
                        .requestMatchers("/pay-success.html").permitAll()
                        .requestMatchers("/pay-fail.html").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/api/**").authenticated() // 其他api需认证
                        .anyRequest().denyAll() // 其他不放行
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .accessDeniedHandler((req, res, ex) -> {
                            String clientIp;
                            String xForwardedFor = req.getHeader("X-Forwarded-For");
                            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                                clientIp = xForwardedFor.split(",")[0].trim();
                            } else {
                                String xRealIp = req.getHeader("X-Real-IP");
                                if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                                    clientIp = xRealIp;
                                } else {
                                    clientIp = req.getRemoteAddr();
                                }
                            }
                            String requestUri = req.getRequestURI();
                            String method = req.getMethod();
                            String userAgent = req.getHeader("User-Agent");
                            String exceptionType = ex.getClass().getSimpleName();
                            log.warn(
                                    "Access denied | " +
                                            "IP: {} | " +
                                            "Method: {} | " +
                                            "URI: {} | " +
                                            "UserAgent: {} | " +
                                            "Exception: {}",
                                    clientIp,
                                    method,
                                    requestUri,
                                    StringUtils.truncate(userAgent, 80),
                                    exceptionType
                            );

                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            try {
                                res.getWriter().write(
                                        new ObjectMapper().writeValueAsString(
                                                ApiResult.error("Access denied")
                                        )
                                );
                            } catch (Exception ioEx) {
                                log.error("Failed to write access denied response", ioEx);
                            }
                        })
                        .authenticationEntryPoint((req, res, ex) -> {
                            String clientIp;
                            String xForwardedFor = req.getHeader("X-Forwarded-For");
                            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                                clientIp = xForwardedFor.split(",")[0].trim();
                            } else {
                                String xRealIp = req.getHeader("X-Real-IP");
                                if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                                    clientIp = xRealIp;
                                } else {
                                    clientIp = req.getRemoteAddr();
                                }
                            }
                            String requestUri = req.getRequestURI();
                            String method = req.getMethod();
                            String userAgent = req.getHeader("User-Agent");
                            String exceptionType = ex.getClass().getSimpleName();

                            log.warn(
                                    "Unauthorized access attempt | " +
                                            "IP: {} | " +
                                            "Method: {} | " +
                                            "URI: {} | " +
                                            "UserAgent: {} | " +
                                            "Exception: {}",
                                    clientIp,
                                    method,
                                    requestUri,
                                    StringUtils.truncate(userAgent, 80),
                                    exceptionType
                            );

                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            try {
                                res.getWriter().write(
                                        new ObjectMapper().writeValueAsString(
                                                ApiResult.error("Unauthorized")
                                        )
                                );
                            } catch (Exception ioEx) {
                                log.error("Failed to write unauthorized response", ioEx);
                            }
                        })
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
