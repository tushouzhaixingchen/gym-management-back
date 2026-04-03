package com.gym.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户名 (登录账号)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码哈希
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * 真实姓名
     */
    @Column(length = 50)
    private String realName;

    /**
     * 角色 ID
     */
    @Column(nullable = false)
    private Integer roleId;

    /**
     * 所属门店 ID
     * 超级管理员可为 null
     */
    @Column(name = "store_id")
    private Integer storeId;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 状态：1正常 0禁用
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    @Builder.Default
    private Integer status = 1;



    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录 IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= 辅助方法 =================

    public String getStatusText() {
        return this.status == 1 ? "正常" : "禁用";
    }

    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return this.roleId != null && this.roleId == 1;
    }


}