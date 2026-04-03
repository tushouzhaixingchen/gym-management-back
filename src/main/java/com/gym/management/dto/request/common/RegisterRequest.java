package com.gym.management.dto.request.common;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequest {

    /**
     * 账号
     * - 管理员注册时：填写 username
     * - 教练/会员注册时：填写 phone (作为登录账号)
     * 注意：后端需根据 roleId 判断哪个字段生效，或者统一逻辑：优先取 phone，无则取 username
     */
    private String account;

    /**
     * 手机号 (用于接收验证码或作为默认账号)
     * 如果是教练/会员注册，此项必填
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String realName;

    /**
     * 🔥 核心：注册角色类型
     * 前端传入：ADMIN, COACH, MEMBER
     * 后端根据此类型校验 roleId 的合法性，防止普通用户注册成超级管理员
     */
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    /**
     * 角色ID
     * - 若 userType=ADMIN, 通常为 2 (门店管理员)，超级管理员通常由数据库初始化，不允许直接注册
     * - 若 userType=COACH, 对应教练角色ID (如 3)
     * - 若 userType=MEMBER, 对应会员角色ID (如 4)
     */
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;

    private String email;

    /**
     * 所属门店ID
     * - 管理员/教练：必填
     * - 会员：可选（取决于业务，是注册即绑定门店，还是购卡时绑定）
     */
    private Integer storeId;

    /**
     * 短信验证码 (建议加上，防止手机号被恶意批量注册)
     * 如果暂时不做短信验证，可先注释掉
     */
    // private String smsCode;
}