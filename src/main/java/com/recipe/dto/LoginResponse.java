package com.recipe.dto;

import com.recipe.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor // 新增：解决无参构造器问题
public class LoginResponse {
    private String token;
    private UserSimpleDTO user; // 用简化DTO，避免返回密码等敏感字段

    // 简化的用户信息DTO
    @Data
    @NoArgsConstructor // 新增：无参构造器（Lombok自动生成）
    public static class UserSimpleDTO {
        private Long id;
        private String username;
        private String email;
        private String avatarUrl;
        private String createdAt;
        private Boolean isAdmin; // 新增：管理员标识字段
    }

    // 静态方法：User转UserSimpleDTO（补充isAdmin字段映射）
    public static UserSimpleDTO convertUserToDTO(User user) {
        UserSimpleDTO dto = new UserSimpleDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());

        // 修复：处理createdAt为空的情况，避免空指针
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().toString());
        } else {
            dto.setCreatedAt(LocalDateTime.now().toString()); // 兜底默认值
        }

        // 新增：映射管理员标识
        dto.setIsAdmin(user.getIsAdmin());

        return dto;
    }
}
