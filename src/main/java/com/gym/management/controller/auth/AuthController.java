package com.gym.management.controller.auth;

import com.gym.management.common.Result;
import com.gym.management.dto.request.admin.ChangePasswordRequest;
import com.gym.management.dto.request.common.LoginRequest;
import com.gym.management.dto.response.LoginResponse;
import com.gym.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 仅处理：登录、刷新令牌、获取当前用户信息
 * ⚠️ 注意：不包含注册功能！注册由管理员在管理端完成。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 统一登录接口
     * 支持：超级管理员、普通管理员、教练、会员
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        LoginResponse response = authService.login(request);
        System.out.println("===== 登录接口被调用 =====");  // 添加这行
        return Result.success(response);
    }

    /**
     * 获取当前登录用户信息
     * 用于前端初始化用户状态、菜单权限等
     * 需要携带 Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public Result<LoginResponse> getCurrentUser() {
        LoginResponse currentUserInfo = authService.getCurrentUser();
        return Result.success(currentUserInfo);
    }

    /**
     * 修改密码
     * 用户登录后修改自己的密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Validated ChangePasswordRequest request) {
        // 后续实现
        authService.changePassword(request);
        return Result.success();
    }
}