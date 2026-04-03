package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码 (用于验证当前用户身份)
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     * 要求：至少6位，建议包含字母和数字
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;
}