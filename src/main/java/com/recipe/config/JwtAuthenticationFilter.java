package com.recipe.config;

import com.recipe.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 构造注入（确保Spring能自动装配）
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // ========== 核心：跳过登录/注册接口（绝对不能拦截！） ==========
        String requestURI = request.getRequestURI();
        if ("/api/user/login".equals(requestURI) || "/api/user/register".equals(requestURI)) {
            filterChain.doFilter(request, response); // 直接放行，不校验Token
            return;
        }

        // ========== 其他接口：校验Token（适配你的JwtUtil） ==========
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        // 1. 提取Token（格式：Bearer <token>）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // 去掉"Bearer "前缀
            username = jwtUtil.extractUsername(jwt); // 从Token提取用户名（邮箱）
        }

        // 2. 校验Token并设置认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 调用你的JwtUtil验证Token
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证信息存入SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 3. 放行请求
        filterChain.doFilter(request, response);
    }
}
