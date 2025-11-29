package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.dto.request.UserUpdateRequest;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.result.UserInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void updateInfo(String id, UserUpdateRequest request) {
        if (request.getUsername() != null) {
            Optional<UserEntity> user = userMapper.findByUsername(request.getUsername());
            if (user.isPresent() && !id.equals(user.get().getId())) {
                throw new BusinessException("Username has been registered");
            }
        }
        UserEntity updateUser = UserEntity.builder()
                .id(id)
                .username(request.getUsername())
                .nickname(request.getNickname())
                .address(request.getAddress())
                .build();
        userMapper.update(updateUser);
    }

    public UserInfoResult getInfo(String id) {
        UserEntity user = userMapper.findById(id).orElseThrow(() -> new BusinessException("User not found"));
        return UserInfoResult.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .icon(user.getIcon())
                .address(user.getAddress())
                .role(user.getRole())
                .build();
    }

    public void updateAvatar(String id, String accessURL) {
        UserEntity updateUser = UserEntity.builder()
                .id(id)
                .icon(accessURL)
                .build();
        userMapper.update(updateUser);
    }
}
