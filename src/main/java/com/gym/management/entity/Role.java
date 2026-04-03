package com.gym.management.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 角色名称 (如：超级管理员, 门店经理, 教练, 会员)
     */
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    /**
     * 角色编码 (用于权限注解，如：ROLE_SUPER_ADMIN, ROLE_COACH)
     * 建议统一格式，方便 Spring Security 识别
     */
    @Column(name = "role_code", nullable = false, length = 50, unique = true)
    private String roleCode;

    /**
     * 角色描述
     */
    @Column(length = 255)
    private String description;

    /**
     * 状态：1启用 0禁用
     */
    @Column(nullable = false)
    @Builder.Default
    private int status = 1;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == 0 && this.status != 1) {
            // 确保默认值为 1 (虽然 @Builder.Default 已处理，但防一手直接 new)
            this.status = 1;
        }
    }

    // ================= 辅助方法 =================

    /**
     * 判断角色是否可用
     * 如果角色被禁用，即使账号密码正确也不应允许登录（可在 Service 层结合此判断）
     */
    public boolean isActive() {
        return this.status == 1;
    }

    /**
     * 获取标准的 Spring Security 角色前缀格式
     * 例如：roleCode = "ADMIN" -> 返回 "ROLE_ADMIN"
     * 如果 roleCode 已经包含 "ROLE_" 前缀，则直接返回
     */
    public String getGrantedAuthority() {
        if (this.roleCode == null) return "";
        if (this.roleCode.startsWith("ROLE_")) {
            return this.roleCode;
        }
        return "ROLE_" + this.roleCode.toUpperCase();
    }
}