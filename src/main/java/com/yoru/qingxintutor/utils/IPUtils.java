package com.yoru.qingxintutor.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IPUtils {
    public static String getClientIpAddress(HttpServletRequest request) {
        // 1. X-Forwarded-For
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            for (String ip : xff.split(",")) {
                ip = ip.trim();
                if (!"unknown".equalsIgnoreCase(ip) && isValidIp(ip)) {
                    return ip;
                }
            }
        }

        // 2. 其他常见代理头
        String ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && isValidIp(ip)) return ip;

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && isValidIp(ip)) return ip;

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && isValidIp(ip)) return ip;

        // 3. remoteAddr
        ip = request.getRemoteAddr();
        return isValidIp(ip) ? ip : "127.0.0.1";
    }

    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        return !ip.equals("0:0:0:0:0:0:0:1") && !ip.equals("127.0.0.1");
    }
}
