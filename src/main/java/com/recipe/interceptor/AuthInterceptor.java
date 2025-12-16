package com.recipe.interceptor;

import com.recipe.common.Result;
import com.recipe.service.UserService;
import com.recipe.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 权限拦截器：
 * 1. 自动解析请求头中的JWT Token，获取用户ID和管理员状态
 * 2. 拦截管理员接口，仅允许管理员访问
 * 3. 将用户信息存入请求属性，控制器可直接获取（无需前端传参）
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    // 拦截器核心方法：请求处理前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ========== 1. 跳过无需登录的接口（白名单） ==========
        String requestURI = request.getRequestURI();
        if (isWhitelist(requestURI)) {
            return true; // 放行白名单接口
        }

        // ========== 2. 解析JWT Token（从请求头获取） ==========
        String token = request.getHeader("Authorization");
        // 处理Bearer Token格式（前端传：Bearer <token>）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Token为空 → 返回未登录
        if (token == null || token.isEmpty()) {
            returnJson(response, Result.error("未登录：请先登录后再操作"));
            return false;
        }

        // 验证Token有效性并解析用户ID
        Long userId;
        try {
            userId = jwtUtil.extractUserId(token); // JwtUtil需实现extractUserId方法
            if (!jwtUtil.validateToken(token, userId)) { // 验证Token是否过期/有效
                returnJson(response, Result.error("登录已过期：请重新登录"));
                return false;
            }
        } catch (Exception e) {
            returnJson(response, Result.error("Token无效：" + e.getMessage()));
            return false;
        }

        // ========== 3. 获取用户权限（是否管理员） ==========
        Boolean isAdmin = userService.isAdmin(userId);

        // ========== 4. 将用户信息存入请求属性（控制器可直接获取） ==========
        request.setAttribute("currentUserId", userId);
        request.setAttribute("isAdmin", isAdmin != null && isAdmin);

        // ========== 5. 拦截管理员专属接口 ==========
        if (isAdminApi(requestURI) && !(isAdmin != null && isAdmin)) {
            returnJson(response, Result.error("无权限：仅管理员可访问该接口"));
            return false;
        }

        // ========== 6. 放行请求 ==========
        return true;
    }

    // ========== 工具方法 ==========
    /**
     * 判断是否为白名单接口（无需登录）
     */
    private boolean isWhitelist(String requestURI) {
        return requestURI.startsWith("/api/users/login")
                || requestURI.startsWith("/api/users/register")
                || requestURI.startsWith("/api/user/login") // 兼容旧路径
                || requestURI.startsWith("/api/user/register");
    }

    /**
     * 判断是否为管理员专属接口
     */
    private boolean isAdminApi(String requestURI) {
        return requestURI.startsWith("/api/users/admin/")
                || requestURI.startsWith("/api/recipes/admin/"); // 可扩展其他管理员接口
    }

    /**
     * 统一返回JSON格式的错误响应
     */
    private void returnJson(HttpServletResponse response, Result<?> result) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value()); // 统一返回200，通过Result.code判断业务状态
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
