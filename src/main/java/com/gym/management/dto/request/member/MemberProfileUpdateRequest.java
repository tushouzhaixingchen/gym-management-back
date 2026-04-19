package com.gym.management.dto.request.member;

import lombok.Data;

import java.time.LocalDate;

/**
 * 会员更新个人信息请求
 */
@Data
public class MemberProfileUpdateRequest {

    /** 真实姓名 */
    private String realName;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    /** 邮箱 */
    private String email;

    /** 出生日期 */
    private LocalDate birthday;

    /** 备注 */
    private String remark;
}
