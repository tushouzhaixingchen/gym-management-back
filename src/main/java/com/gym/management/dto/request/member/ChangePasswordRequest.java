package com.gym.management.dto.request.member;

import lombok.Data;

/**
 * 会员修改密码请求
 */
@Data
public class ChangePasswordRequest {

    /** 旧密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
