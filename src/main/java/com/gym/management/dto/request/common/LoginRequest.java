// dto/request/LoginRequest.java
package com.gym.management.dto.request.common;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "账号不能为空")
    private String account;  // 可以是 username 或 phone

    @NotBlank(message = "密码不能为空")
    private String password;
}