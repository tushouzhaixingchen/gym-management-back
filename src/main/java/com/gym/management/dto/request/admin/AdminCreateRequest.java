// dto/request/AdminCreateRequest.java
package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AdminCreateRequest {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "账号 4-20 位字母或数字")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;

    private String realName;

    @NotNull(message = "角色不能为空")
    private Integer roleId;

    private Integer storeId;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}