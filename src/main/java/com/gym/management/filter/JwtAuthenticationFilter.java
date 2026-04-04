package com.gym.management.filter;

import com.gym.management.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 获取 Header
        String header = request.getHeader("Authorization");

        // 2. 校验格式 (Bearer <token>)
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // 3. 验证 Token 有效性
        if (!jwtUtil.validateToken(token)) {
            log.warn("无效的 JWT Token，请求路径: {}", request.getRequestURI());
            // 注意：这里通常不直接返回 401，而是让后面的 SecurityExceptionHandling 处理
            // 或者你可以选择直接在这里阻断：
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 无效");
            // return;
            // 为了保持 Spring Security 标准流程，我们继续放行，但 SecurityContext 为空
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 4. 解析用户信息 (使用更新后的方法)
            Integer userId = jwtUtil.getUserIdFromToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);
            String account = jwtUtil.getAccountFromToken(token);

            log.info("【JWT 调试】Token 解析结果 - userId: {}, userType: {}, account: {}", 
                userId, userType, account);

            if (userId == null || !StringUtils.hasText(userType)) {
                log.error("Token 中缺少必要信息 (userId 或 userType)");
                filterChain.doFilter(request, response);
                return;
            }

            // 5. 根据用户类型动态分配角色
            String rolePrefix = "ROLE_";
            String roleName = rolePrefix + userType; // 例如: ROLE_ADMIN, ROLE_COACH, ROLE_MEMBER

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

            // 6. 构建 Authentication 对象
            // principal: 使用 userId (Integer)，这样 getCurrentAdmin() 才能直接获取到用户 ID
            // credentials: 密码留空 (因为已经验证过 Token 了)
            // authorities: 动态生成的角色
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,  // 🔴 使用 userId (Integer) 作为 principal
                            null,
                            Collections.singletonList(authority)
                    );

            log.info("【JWT 调试】Authentication 创建成功 - Principal 类型：{}, Principal 值：{}", 
                authentication.getPrincipal().getClass().getName(), authentication.getPrincipal());

            // 7. 存入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("认证成功 | ID: {} | 类型: {} | 角色: {} | 路径: {}",
                    userId, userType, roleName, request.getRequestURI());

        } catch (Exception e) {
            log.error("解析 Token 时发生异常: {}", e.getMessage(), e);
            // 解析失败不阻断请求，让后续逻辑处理（通常会报未授权）
        }

        filterChain.doFilter(request, response);
    }
}