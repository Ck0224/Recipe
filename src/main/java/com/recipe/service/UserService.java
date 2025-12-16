package com.recipe.service;

import com.recipe.entity.User;

import java.util.List;

/**
 * 用户服务接口（完整版：包含权限/密码/头像/管理员功能）
 */
public interface UserService {
    // ========== 基础功能 ==========
    User register(User user);
    String login(String email, String password);
    User findByEmail(String email);
    User updateUserInfo(Long userId, User user);

    // ========== 核心新增：权限/密码/头像相关 ==========
    /**
     * 判断用户是否为管理员
     * @param userId 用户ID
     * @return true=管理员，false=普通用户
     */
    Boolean isAdmin(Long userId);

    /**
     * 修改密码（带旧密码校验）
     * @param userId 用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户头像URL
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 按ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User findById(Long userId);

    /**
     * 管理员查询所有用户（支持筛选管理员）
     * @param isAdmin 筛选条件：true=仅管理员，false=仅普通用户，null=所有用户
     * @return 用户列表
     */
    List<User> getAllUsers(Boolean isAdmin);

    /**
     * 管理员修改用户的管理员权限
     * @param operatorId 操作人ID（必须是管理员）
     * @param targetUserId 目标用户ID
     * @param isAdmin 新的管理员状态
     */
    void updateUserAdminStatus(Long operatorId, Long targetUserId, Boolean isAdmin);
}
