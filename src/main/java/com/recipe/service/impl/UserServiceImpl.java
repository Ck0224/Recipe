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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(User user) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 保存用户
        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        // 认证用户
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        // 生成Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtUtil.generateToken(userDetails, user.getId());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public User updateUserInfo(Long userId, User user) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 更新字段（仅允许更新非敏感信息）
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getPreferences() != null) {
            existingUser.setPreferences(user.getPreferences());
        }
        return userRepository.save(existingUser);
    }
}