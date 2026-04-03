package com.gym.management.service;

import com.gym.management.dto.request.common.LoginRequest;
import com.gym.management.dto.request.admin.ChangePasswordRequest; // 假设你会有这个 DTO
import com.gym.management.dto.response.LoginResponse;

/**
 * 认证服务接口
 * 职责：仅处理与“身份验证”相关的逻辑（登录、获取当前用户、改密、登出）
 * 注意：用户注册/创建逻辑已移至 UserService
 */
public interface AuthService {

    /**
     * 用户登录
     * @param request 包含用户名和密码
     * @return 包含 Token 和用户基本信息的响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前登录用户信息
     * 从 SecurityContext 中解析当前用户，并返回详细信息
     * @return 当前用户信息
     */
    LoginResponse getCurrentUser();

    /**
     * 修改当前用户密码
     * @param request 包含旧密码和新密码
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * (可选) 登出
     * 如果使用无状态 JWT，通常前端删除 Token 即可，后端可记录黑名单
     */
    void logout(String token);
}