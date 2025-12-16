package com.recipe.controller;

import com.recipe.common.Result;
import com.recipe.dto.LoginRequest;
import com.recipe.dto.LoginResponse;
import com.recipe.entity.User;
import com.recipe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器（完整功能：登录/注册 + 密码修改 + 头像更新 + 管理员用户管理）
 */
@RestController
@RequestMapping("/api/users") // 规范路径：复数形式 /api/users
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // ========== 基础功能：登录/注册 ==========
    /**
     * 用户登录（标准路径：/api/users/login）
     */
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            String jwt = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
            User user = userService.findByEmail(loginRequest.getEmail());
            LoginResponse.UserSimpleDTO userDTO = LoginResponse.convertUserToDTO(user);
            LoginResponse loginResponse = new LoginResponse(jwt, userDTO);
            return ResponseEntity.ok(Result.success(loginResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("登录失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("系统异常：登录失败"));
        }
    }

    /**
     * 用户注册（标准路径：/api/users/register）
     */
    @PostMapping("/register")
    public ResponseEntity<Result<LoginResponse.UserSimpleDTO>> register(@RequestBody User user) {
        try {
            User savedUser = userService.register(user);
            LoginResponse.UserSimpleDTO userDTO = LoginResponse.convertUserToDTO(savedUser);
            return ResponseEntity.ok(Result.success(userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("注册失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("系统异常：注册失败"));
        }
    }

    // ========== 个人中心功能 ==========
    @PutMapping("/{userId}/password")
    public ResponseEntity<Result<String>> updatePassword(
            @PathVariable Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            userService.updatePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(Result.success("密码修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("密码修改失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{userId}/avatar")
    public ResponseEntity<Result<String>> updateAvatar(
            @PathVariable Long userId,
            @RequestParam String avatarUrl) {
        try {
            userService.updateAvatar(userId, avatarUrl);
            return ResponseEntity.ok(Result.success("头像更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("头像更新失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Result<LoginResponse.UserSimpleDTO>> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody User user) {
        try {
            User updatedUser = userService.updateUserInfo(userId, user);
            LoginResponse.UserSimpleDTO userDTO = LoginResponse.convertUserToDTO(updatedUser);
            return ResponseEntity.ok(Result.success(userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("信息更新失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Result<LoginResponse.UserSimpleDTO>> getUserInfo(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            LoginResponse.UserSimpleDTO userDTO = LoginResponse.convertUserToDTO(user);
            return ResponseEntity.ok(Result.success(userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("查询用户信息失败：" + e.getMessage()));
        }
    }

    // ========== 管理员功能 ==========
    @GetMapping("/admin/list")
    public ResponseEntity<Result<List<LoginResponse.UserSimpleDTO>>> getAllUsers(
            @RequestParam Long operatorId,
            @RequestParam(required = false) Boolean isAdmin) {
        try {
            Boolean operatorIsAdmin = userService.isAdmin(operatorId);
            if (!Boolean.TRUE.equals(operatorIsAdmin)) {
                return ResponseEntity.ok(Result.error("无权限：仅管理员可查询用户列表"));
            }
            List<User> users = userService.getAllUsers(isAdmin);
            List<LoginResponse.UserSimpleDTO> userDTOS = users.stream()
                    .map(LoginResponse::convertUserToDTO)
                    .toList();
            return ResponseEntity.ok(Result.success(userDTOS));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("查询用户列表失败：" + e.getMessage()));
        }
    }

    @PutMapping("/admin/{targetUserId}/status")
    public ResponseEntity<Result<String>> updateUserAdminStatus(
            @RequestParam Long operatorId,
            @PathVariable Long targetUserId,
            @RequestParam Boolean isAdmin) {
        try {
            userService.updateUserAdminStatus(operatorId, targetUserId, isAdmin);
            String msg = isAdmin ? "设置为管理员成功" : "取消管理员权限成功";
            return ResponseEntity.ok(Result.success(msg));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error("修改用户权限失败：" + e.getMessage()));
        }
    }

    // ========== 兼容旧路径（可选：映射到不同URL，避免冲突） ==========
    // 若需兼容旧路径 /api/user/login，改为：
    @PostMapping("/user/login") // 路径改为 /api/users/user/login，避免冲突
    public ResponseEntity<Result<LoginResponse>> loginCompat(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @PostMapping("/user/register") // 路径改为 /api/users/user/register
    public ResponseEntity<Result<LoginResponse.UserSimpleDTO>> registerCompat(@RequestBody User user) {
        return register(user);
    }
}

