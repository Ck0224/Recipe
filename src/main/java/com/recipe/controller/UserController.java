package com.recipe.controller;

import com.recipe.common.Result;
import com.recipe.dto.LoginRequest;
import com.recipe.dto.LoginResponse;
import com.recipe.entity.User;
import com.recipe.repository.UserRepository;
import com.recipe.util.JwtUtil;
import com.recipe.service.impl.UserDetailsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 用户控制器（优化点：统一返回格式 + 异常捕获 + 注册校验 + 敏感信息屏蔽）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 构造注入所有依赖（Spring自动装配）
    public UserController(
            AuthenticationManager authenticationManager,
            UserDetailsServiceImpl userDetailsService,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录（优化：统一Result返回 + 捕获异常 + 屏蔽敏感字段）
     */
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. 校验请求参数
            if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
                return ResponseEntity.ok(Result.error("邮箱不能为空"));
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return ResponseEntity.ok(Result.error("密码不能为空"));
            }

            // 2. Spring Security验证账号密码
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 3. 加载用户信息（避免返回密码等敏感字段）
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

            // 4. 生成JWT Token
            String jwt = jwtUtil.generateToken(userDetails, user.getId());

            // 5. 构建响应（用简化DTO，屏蔽密码）
            LoginResponse.UserSimpleDTO userDTO = LoginResponse.convertUserToDTO(user);
            LoginResponse loginResponse = new LoginResponse(jwt, userDTO);

            return ResponseEntity.ok(Result.success(loginResponse));

        } catch (BadCredentialsException e) {
            // 账号密码错误
            return ResponseEntity.ok(Result.error("邮箱或密码错误"));
        } catch (RuntimeException e) {
            // 业务异常
            return ResponseEntity.ok(Result.error(e.getMessage()));
        } catch (Exception e) {
            // 系统异常
            return ResponseEntity.ok(Result.error("登录失败：" + e.getMessage()));
        }
    }

    /**
     * 用户注册（优化：参数校验 + 重复邮箱校验 + 统一返回格式）
     */
    @PostMapping("/register")
    public ResponseEntity<Result<LoginResponse.UserSimpleDTO>> register(@RequestBody User user) {
        try {
            // 1. 校验必填参数
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                return ResponseEntity.ok(Result.error("用户名不能为空"));
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.ok(Result.error("邮箱不能为空"));
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.ok(Result.error("密码不能为空"));
            }

            // 2. 校验邮箱是否已存在
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.ok(Result.error("该邮箱已注册"));
            }

            // 3. 密码加密 + 补充默认值
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAvatarUrl(user.getAvatarUrl() == null ? "" : user.getAvatarUrl());
            user.setPreferences("{}"); // 默认空JSON
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // 4. 保存用户
            User savedUser = userRepository.save(user);

            // 5. 转换为简化DTO返回（屏蔽密码）
            LoginResponse.UserSimpleDTO userDTO = LoginResponse.convertUserToDTO(savedUser);
            return ResponseEntity.ok(Result.success(userDTO));

        } catch (RuntimeException e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("注册失败：" + e.getMessage()));
        }
    }
}
