package com.yoru.qingxintutor.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoru.qingxintutor.config.SpringAwareConfigurator;
import com.yoru.qingxintutor.mapper.ForumMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.request.MessageCreateRequest;
import com.yoru.qingxintutor.pojo.result.ForumMessageInfoResult;
import com.yoru.qingxintutor.service.ForumMessageService;
import com.yoru.qingxintutor.utils.JwtUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/forum/{forumId}/message", configurator = SpringAwareConfigurator.class)
public class ForumMessageWebSocket {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ForumMessageService forumMessageService;
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtils jwtUtils;

    private static final String USER_ID = "userId";
    private static final Map<Long, Set<Session>> FORUM_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(@PathParam("forumId") Long forumId, Session session, EndpointConfig config) {
        try {
            HandshakeRequest request = (HandshakeRequest) config.getUserProperties().get("httpRequest");
            if (request != null) {
                List<String> authHeaders = request.getHeaders().get("Authorization");
                String token = null;
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String authHeader = authHeaders.get(0);
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }

                if (token != null && jwtUtils.validateToken(token)) {
                    String userId = jwtUtils.getUserIdFromToken(token);
                    if (userMapper.findById(userId).isPresent() && forumMapper.findById(forumId).isPresent()) {
                        session.getUserProperties().put(USER_ID, userId);
                        FORUM_SESSIONS.computeIfAbsent(forumId, k -> ConcurrentHashMap.newKeySet()).add(session);
                        log.debug("User {} authenticated websocket for forum {}", userId, forumId);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        // 认证失败，关闭连接
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unauthorized"));
        } catch (IOException ignored) {
        }
    }

    @OnMessage
    public void onMessage(String text, @PathParam("forumId") Long forumId, Session session) {
        try {
            MessageCreateRequest forumMessageCreateRequest = objectMapper.readValue(text, MessageCreateRequest.class);
            ForumMessageInfoResult msg = forumMessageService.insert((String) session.getUserProperties().get(USER_ID),
                    forumId, forumMessageCreateRequest);

            // 广播给同论坛所有人
            String response = objectMapper.writeValueAsString(msg);
            broadcast(forumId, response);
        } catch (Exception e) {
            log.error("Failed when handle websocket message: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(@PathParam("forumId") Long forumId, Session session) {
        Set<Session> sessions = FORUM_SESSIONS.get(forumId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) FORUM_SESSIONS.remove(forumId);
            log.debug("User {} disconnect websocket for forum {}", session.getUserProperties().get(USER_ID), forumId);
        }
    }

    @OnError
    public void onError(@PathParam("forumId") Long forumId, Session session, Throwable error) {
        Set<Session> sessions = FORUM_SESSIONS.get(forumId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) FORUM_SESSIONS.remove(forumId);
        }
        log.error("WebSocket error: {}", error.getMessage());
    }

    public void broadcast(Long forumId, String message) {
        FORUM_SESSIONS.getOrDefault(forumId, Set.of())
                .forEach(s -> {
                    try {
                        if (s.isOpen()) s.getBasicRemote().sendText(message);
                    } catch (IOException ignored) {
                    }
                });
    }
}