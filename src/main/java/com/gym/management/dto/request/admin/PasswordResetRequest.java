// dto/request/PasswordResetRequest.java
package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class PasswordResetRequest {

    @NotNull(message = "管理员 ID 不能为空")
    private Integer adminId;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String newPassword;
}