package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.PrivateChatMapper;
import com.yoru.qingxintutor.mapper.PrivateMessageMapper;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.MessageCreateRequest;
import com.yoru.qingxintutor.pojo.entity.PrivateChatEntity;
import com.yoru.qingxintutor.pojo.entity.PrivateMessageEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.result.PrivateChatInfoResult;
import com.yoru.qingxintutor.pojo.result.PrivateMessageInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PrivateChatService {
    @Autowired
    private PrivateChatMapper chatMapper;
    @Autowired
    private PrivateMessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @Transactional
    public PrivateChatInfoResult getOrCreateChat(String studentId, Long teacherId) {
        String studentName = userMapper.findById(studentId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        if (!teacherMapper.existsById(teacherId))
            throw new BusinessException("Teacher not found");
        Optional<PrivateChatEntity> chatOptional = chatMapper.findByStudentAndTeacher(studentId, teacherId);
        if (chatOptional.isEmpty()) {
            PrivateChatEntity chat = PrivateChatEntity.builder()
                    .userId(studentId)
                    .teacherId(teacherId)
                    .createTime(LocalDateTime.now())
                    .build();
            chatMapper.insert(chat);
            PrivateMessageEntity message = PrivateMessageEntity.builder()
                    .chatId(chat.getId())
                    .content("学生向您发起了对话")
                    .sender(PrivateMessageEntity.SenderType.STUDENT)
                    .createTime(LocalDateTime.now())
                    .build();
            messageMapper.insert(message);
            return entityToResult(chat, studentName,
                    teacherMapper.findNameById(teacherId)
                            .orElseThrow(() -> new BusinessException("Teacher not found")));
        } else {
            return entityToResult(chatOptional.get(), studentName,
                    teacherMapper.findNameById(teacherId)
                            .orElseThrow(() -> new BusinessException("Teacher not found")));
        }
    }

    @Transactional
    public PrivateMessageInfoResult insert(String userId, Long chatId, MessageCreateRequest messageCreateRequest) {
        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        PrivateChatEntity chat = chatMapper.findById(chatId)
                .orElseThrow(() -> new BusinessException("Chat not found"));
        if (user.getRole() == UserEntity.Role.STUDENT) {
            if (!chat.getUserId().equals(user.getId()))
                throw new BusinessException("Chat not found");
            PrivateMessageEntity message = PrivateMessageEntity.builder()
                    .chatId(chatId)
                    .content(messageCreateRequest.getContent())
                    .sender(PrivateMessageEntity.SenderType.STUDENT)
                    .createTime(LocalDateTime.now())
                    .build();
            messageMapper.insert(message);
            return entityToResult(message, entityToResult(chat, user.getUsername(),
                    teacherMapper.findNameById(chat.getTeacherId())
                            .orElseThrow(() -> new BusinessException("Teacher not found")))
            );
        } else {    // TEACHER
            Long teacherId = teacherMapper.findTeacherIdByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            if (!chat.getTeacherId().equals(teacherId))
                throw new BusinessException("Chat not found");
            PrivateMessageEntity message = PrivateMessageEntity.builder()
                    .chatId(chatId)
                    .content(messageCreateRequest.getContent())
                    .sender(PrivateMessageEntity.SenderType.TEACHER)
                    .createTime(LocalDateTime.now())
                    .build();
            messageMapper.insert(message);
            return entityToResult(message, entityToResult(chat,
                    userMapper.findById(chat.getUserId())
                            .orElseThrow(() -> new BusinessException("User not found"))
                            .getUsername(),
                    teacherMapper.findNameById(teacherId)
                            .orElseThrow(() -> new BusinessException("Teacher not found")))
            );
        }
    }

    public List<PrivateChatInfoResult> listAll(String userId) {
        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.getRole() == UserEntity.Role.STUDENT) {
            return chatMapper.findByStudent(userId)
                    .stream()
                    .map(chat -> entityToResult(chat, user.getUsername(),
                            teacherMapper.findNameById(chat.getTeacherId())
                                    .orElseThrow(() -> new BusinessException("Teacher not found"))))
                    .toList();
        } else {    // TEACHER
            Long teacherId = teacherMapper.findTeacherIdByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            String teacherName = teacherMapper.findNameById(teacherId)
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            return chatMapper.findByTeacher(teacherId)
                    .stream()
                    .map(chat -> entityToResult(chat,
                            userMapper.findById(chat.getUserId())
                                    .orElseThrow(() -> new BusinessException("User not found"))
                                    .getUsername(),
                            teacherName))
                    .toList();
        }
    }

    public PrivateChatInfoResult findById(String userId, Long chatId) {
        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        PrivateChatEntity chat = chatMapper.findById(chatId)
                .orElseThrow(() -> new BusinessException("Chat not found"));
        if (user.getRole() == UserEntity.Role.STUDENT) {
            if (!chat.getUserId().equals(user.getId()))
                throw new BusinessException("Chat not found");

            return entityToResult(chat, user.getUsername(),
                    teacherMapper.findNameById(chat.getTeacherId())
                            .orElseThrow(() -> new BusinessException("Teacher not found"))
            );
        } else {    // TEACHER
            Long teacherId = teacherMapper.findTeacherIdByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            if (!chat.getTeacherId().equals(teacherId))
                throw new BusinessException("Chat not found");
            return entityToResult(chat,
                    userMapper.findById(chat.getUserId())
                            .orElseThrow(() -> new BusinessException("User not found"))
                            .getUsername(),
                    teacherMapper.findNameById(teacherId)
                            .orElseThrow(() -> new BusinessException("Teacher not found"))
            );
        }
    }

    public List<PrivateMessageInfoResult> listMessagesByChatId(String userId, Long chatId) {
        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        PrivateChatEntity chat = chatMapper.findById(chatId)
                .orElseThrow(() -> new BusinessException("Chat not found"));
        String teacherName = teacherMapper.findNameById(chat.getTeacherId())
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        if (user.getRole() == UserEntity.Role.STUDENT) {
            if (!chat.getUserId().equals(user.getId()))
                throw new BusinessException("Chat not found");
            return messageMapper.findByChatId(chatId)
                    .stream()
                    .map(message -> entityToResult(message, entityToResult(chat, user.getUsername(), teacherName)
                    ))
                    .toList();
        } else {    // TEACHER
            Long teacherId = teacherMapper.findTeacherIdByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            if (!chat.getTeacherId().equals(teacherId))
                throw new BusinessException("Chat not found");
            String studentName = userMapper.findById(chat.getUserId())
                    .orElseThrow(() -> new BusinessException("User not found"))
                    .getUsername();
            return messageMapper.findByChatId(chatId)
                    .stream()
                    .map(message -> entityToResult(message, entityToResult(chat, studentName, teacherName)
                    ))
                    .toList();
        }
    }

    public PrivateMessageInfoResult findMessageById(String userId, Long id) {
        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        PrivateMessageEntity message = messageMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Message not found"));
        PrivateChatEntity chat = chatMapper.findById(message.getChatId())
                .orElseThrow(() -> new BusinessException("Chat not found"));
        String teacherName = teacherMapper.findNameById(chat.getTeacherId())
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        if (user.getRole() == UserEntity.Role.STUDENT) {
            if (!chat.getUserId().equals(user.getId()) && message.getSender() != PrivateMessageEntity.SenderType.STUDENT)
                throw new BusinessException("Chat not found");
            return entityToResult(message, entityToResult(chat, user.getUsername(), teacherName));
        } else {    // TEACHER
            Long teacherId = teacherMapper.findTeacherIdByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            if (!chat.getTeacherId().equals(teacherId) && message.getSender() != PrivateMessageEntity.SenderType.TEACHER)
                throw new BusinessException("Chat not found");
            String studentName = userMapper.findById(chat.getUserId())
                    .orElseThrow(() -> new BusinessException("User not found"))
                    .getUsername();
            return entityToResult(message, entityToResult(chat, studentName, teacherName));
        }
    }

    @Transactional(readOnly = true)
    public List<PrivateMessageInfoResult> listMessagesByUserId(String userId) {
        UserEntity user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        List<PrivateMessageInfoResult> list = new ArrayList<>();
        if (user.getRole() == UserEntity.Role.STUDENT) {
            for (var chat : chatMapper.findByStudent(userId)) {
                PrivateChatInfoResult chatResult = entityToResult(chat, user.getUsername(),
                        teacherMapper.findNameById(chat.getTeacherId())
                                .orElseThrow(() -> new BusinessException("Teacher not found")));
                list.addAll(messageMapper.findByChatId(chat.getId())
                        .stream()
                        .filter(message -> message.getSender() == PrivateMessageEntity.SenderType.STUDENT)
                        .map(message -> entityToResult(message, chatResult))
                        .toList()
                );
            }
        } else {    // TEACHER
            Long teacherId = teacherMapper.findTeacherIdByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            String teacherName = teacherMapper.findNameById(teacherId)
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            for (var chat : chatMapper.findByTeacher(teacherId)) {
                PrivateChatInfoResult chatResult = entityToResult(chat,
                        userMapper.findById(chat.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername(),
                        teacherName);
                list.addAll(messageMapper.findByChatId(chat.getId())
                        .stream()
                        .filter(message -> message.getSender() == PrivateMessageEntity.SenderType.TEACHER)
                        .map(message -> entityToResult(message, chatResult))
                        .toList()
                );
            }
        }
        return list;
    }


    private static PrivateChatInfoResult entityToResult(PrivateChatEntity entity, String studentName, String teacherName) {
        return PrivateChatInfoResult.builder()
                .id(entity.getId())
                .studentId(entity.getUserId())
                .studentName(studentName)
                .teacherId(entity.getTeacherId())
                .teacherName(teacherName)
                .build();
    }

    private static PrivateMessageInfoResult entityToResult(PrivateMessageEntity entity, PrivateChatInfoResult chat) {
        return PrivateMessageInfoResult.builder()
                .id(entity.getId())
                .chat(chat)
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .sender(entity.getSender())
                .build();
    }
}
