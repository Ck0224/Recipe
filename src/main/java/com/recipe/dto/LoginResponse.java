package com.recipe.dto;

import com.recipe.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserSimpleDTO user; // 用简化DTO，避免返回密码等敏感字段

    // 简化的用户信息DTO
    @Data
    public static class UserSimpleDTO {
        private Long id;
        private String username;
        private String email;
        private String avatarUrl;
        private String createdAt;
    }

    // 静态方法：User转UserSimpleDTO
    public static UserSimpleDTO convertUserToDTO(User user) {
        UserSimpleDTO dto = new UserSimpleDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt().toString());
        return dto;
    }
}