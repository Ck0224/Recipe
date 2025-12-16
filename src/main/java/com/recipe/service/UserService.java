package com.recipe.service;

import com.recipe.entity.User;

public interface UserService {
    User register(User user);
    String login(String email, String password);
    User findByEmail(String email);
    User updateUserInfo(Long userId, User user);
}
