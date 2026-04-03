package com.gym.management.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户的信息
 */
@Slf4j
@Component
public class UserContext {

    /**
     * 获取当前登录用户的认证信息
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 获取当前登录用户的 ID
     * @return 用户 ID，未登录返回 null
     */
    public static Integer getCurrentUserId() {
        return getAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof UserDetails)
                .map(auth -> {
                    GymUserDetails userDetails = (GymUserDetails) auth.getPrincipal();
                    return userDetails.getUserId();
                })
                .orElse(null);
    }

    /**
     * 获取当前登录用户的用户名
     * @return 用户名，未登录返回 null
     */
    public static String getCurrentUserName() {
        return getAuthentication()
                .map(auth -> {
                    Object principal = auth.getPrincipal();
                    // 情况 1：principal 是 String 类型（直接就是用户名）
                    if (principal instanceof String) {
                        return (String) principal;
                    }
                    // 情况 2：principal 是 UserDetails 类型
                    if (principal instanceof UserDetails) {
                        return ((UserDetails) principal).getUsername();
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * 获取当前登录用户的门店 ID
     * @return 门店 ID，未登录或无门店返回 null
     */
    public static Integer getCurrentUserStoreId() {
        return getAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof GymUserDetails)
                .map(auth -> {
                    GymUserDetails userDetails = (GymUserDetails) auth.getPrincipal();
                    return userDetails.getStoreId();
                })
                .orElse(null);
    }

    /**
     * 获取当前登录用户的角色列表
     * @return 角色列表，未登录返回空列表
     */
    public static java.util.List<String> getCurrentUserRoles() {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .toList())
                .orElse(java.util.Collections.emptyList());
    }

    /**
     * 检查当前用户是否已登录
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof UserDetails)
                .isPresent();
    }

    /**
     * 检查当前用户是否有指定角色
     * @param role 角色名（如：ROLE_ADMIN）
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * 清空当前用户上下文（用于登出）
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
        log.info("用户上下文已清空");
    }
}