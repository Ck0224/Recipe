package com.recipe.service.impl;

import com.recipe.entity.User;
import com.recipe.repository.UserRepository;
import com.recipe.service.UserService;
import com.recipe.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    // 1. 注册（增强：默认非管理员+字段校验）
    @Override
    @Transactional
    public User register(User user) {
        // 参数校验
        Assert.notNull(user, "用户信息不能为空");
        Assert.hasText(user.getEmail(), "邮箱不能为空");
        Assert.hasText(user.getPassword(), "密码不能为空");
        Assert.hasText(user.getUsername(), "用户名不能为空");

        // 检查邮箱/用户名是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已被占用");
        }

        // 初始化管理员标识（默认非管理员）
        if (user.getIsAdmin() == null) {
            user.setIsAdmin(false);
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        return userRepository.save(user);
    }

    // 2. 登录（原有逻辑不变）
    @Override
    public String login(String email, String password) {
        Assert.hasText(email, "邮箱不能为空");
        Assert.hasText(password, "密码不能为空");

        // 认证用户
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        // 生成Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("用户不存在"));
        return jwtUtil.generateToken(userDetails, user.getId());
    }

    // 3. 按邮箱查询用户（原有逻辑+参数校验）
    @Override
    public User findByEmail(String email) {
        Assert.hasText(email, "邮箱不能为空");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在，邮箱：" + email));
    }

    // 4. 更新用户信息（原有逻辑+增强）
    @Override
    @Transactional
    public User updateUserInfo(Long userId, User user) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(user, "用户信息不能为空");

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID：" + userId));

        // 仅更新非敏感字段（用户名/头像/偏好）
        if (StringUtils.hasText(user.getUsername())) {
            // 用户名唯一校验
            if (!existingUser.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(user.getUsername())) {
                throw new RuntimeException("用户名已被占用");
            }
            existingUser.setUsername(user.getUsername());
        }
        if (StringUtils.hasText(user.getAvatarUrl())) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getPreferences() != null) {
            existingUser.setPreferences(user.getPreferences());
        }

        return userRepository.save(existingUser);
    }

    // ========== 核心新增：管理员/密码/头像相关方法 ==========
    // 5. 判断用户是否为管理员
    @Override
    public Boolean isAdmin(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        Boolean isAdmin = userRepository.isAdmin(userId);
        return isAdmin == null ? false : isAdmin;
    }

    // 6. 修改密码（带旧密码校验）
    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.hasText(oldPassword, "旧密码不能为空");
        Assert.hasText(newPassword, "新密码不能为空");

        // 校验新密码复杂度（示例：至少6位）
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID：" + userId));

        // 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 加密新密码并更新
        String encryptedNewPwd = passwordEncoder.encode(newPassword);
        int affectedRows = userRepository.updatePassword(userId, encryptedNewPwd);

        if (affectedRows == 0) {
            throw new RuntimeException("密码修改失败");
        }
    }

    // 7. 上传/更新头像（轻量化操作）
    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.hasText(avatarUrl, "头像URL不能为空");

        int affectedRows = userRepository.updateAvatarUrl(userId, avatarUrl);

        if (affectedRows == 0) {
            throw new RuntimeException("头像更新失败，用户不存在");
        }
    }

    // 8. 管理员查询所有用户（支持筛选管理员）
    @Override
    public List<User> getAllUsers(Boolean isAdmin) {
        if (isAdmin == null) {
            return userRepository.findAllUsers();
        } else {
            return userRepository.findUsersByAdminStatus(isAdmin);
        }
    }

    // 9. 按ID查询用户（新增：基础查询）
    @Override
    public User findById(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID：" + userId));
    }

    // 10. 管理员修改用户权限（新增：仅管理员可操作）
    @Override
    @Transactional
    public void updateUserAdminStatus(Long operatorId, Long targetUserId, Boolean isAdmin) {
        // 校验操作人是否为管理员
        Boolean operatorIsAdmin = isAdmin(operatorId);
        if (!Boolean.TRUE.equals(operatorIsAdmin)) {
            throw new RuntimeException("无权限修改用户权限（仅管理员可操作）");
        }

        // 校验目标用户存在
        User targetUser = findById(targetUserId);

        // 更新管理员标识
        targetUser.setIsAdmin(isAdmin);
        userRepository.save(targetUser);
    }
}
