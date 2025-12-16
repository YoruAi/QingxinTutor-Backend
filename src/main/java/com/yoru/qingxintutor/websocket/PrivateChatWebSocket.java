package com.yoru.qingxintutor.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoru.qingxintutor.config.SpringAwareConfigurator;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.PrivateChatMapper;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.MessageCreateRequest;
import com.yoru.qingxintutor.pojo.entity.PrivateChatEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.result.PrivateMessageInfoResult;
import com.yoru.qingxintutor.service.PrivateChatService;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/private-chat/{chatId}/message", configurator = SpringAwareConfigurator.class)
public class PrivateChatWebSocket {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PrivateChatService privateChatService;
    @Autowired
    private PrivateChatMapper chatMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private JwtUtils jwtUtils;

    private static final String USER_ID = "userId";
    private static final Map<Long, Set<Session>> PRIVATE_CHAT_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(@PathParam("chatId") Long chatId, Session session, EndpointConfig config) {
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
                    Optional<UserEntity> userOptional = userMapper.findById(userId);
                    Optional<PrivateChatEntity> chatOptional = chatMapper.findById(chatId);
                    if (userOptional.isPresent() && chatOptional.isPresent()) {
                        UserEntity user = userOptional.get();
                        PrivateChatEntity chat = chatOptional.get();
                        if (chat.getUserId().equals(user.getId()) ||
                                chat.getTeacherId().equals(teacherMapper.findTeacherIdByUserId(userId)
                                        .orElseThrow(() -> new BusinessException("Teacher not found")))) {
                            session.getUserProperties().put(USER_ID, userId);
                            PRIVATE_CHAT_SESSIONS.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(session);
                            log.debug("User {} authenticated websocket for chat {}", userId, chatId);
                            return;
                        }
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
    public void onMessage(String text, @PathParam("chatId") Long chatId, Session session) {
        try {
            MessageCreateRequest messageCreateRequest = objectMapper.readValue(text, MessageCreateRequest.class);
            PrivateMessageInfoResult msg = privateChatService.insert((String) session.getUserProperties().get(USER_ID),
                    chatId, messageCreateRequest);

            // 广播给同对话所有人
            String response = objectMapper.writeValueAsString(msg);
            broadcast(chatId, response);
        } catch (Exception e) {
            log.error("Failed when handle websocket message: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(@PathParam("chatId") Long chatId, Session session) {
        Set<Session> sessions = PRIVATE_CHAT_SESSIONS.get(chatId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) PRIVATE_CHAT_SESSIONS.remove(chatId);
            log.debug("User {} disconnect websocket for chat {}", session.getUserProperties().get(USER_ID), chatId);
        }
    }

    @OnError
    public void onError(@PathParam("chatId") Long chatId, Session session, Throwable error) {
        Set<Session> sessions = PRIVATE_CHAT_SESSIONS.get(chatId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) PRIVATE_CHAT_SESSIONS.remove(chatId);
        }
        log.error("WebSocket error: {}", error.getMessage());
    }

    public void broadcast(Long chatId, String message) {
        PRIVATE_CHAT_SESSIONS.getOrDefault(chatId, Set.of())
                .forEach(s -> {
                    try {
                        if (s.isOpen()) s.getBasicRemote().sendText(message);
                    } catch (IOException ignored) {
                    }
                });
    }
}
