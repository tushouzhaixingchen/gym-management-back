// dto/request/AdminUpdateRequest.java
package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AdminUpdateRequest {

    @NotNull(message = "ID 不能为空")
    private Integer id;

    private String realName;
    private Integer roleId;
    private Integer storeId;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer status;

    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String newPassword;
}