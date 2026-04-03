package com.gym.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教练实体类
 * 对应数据库表：coaches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coaches")
public class Coach {

    /**
     * 教练ID (主键)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 教练工号（全系统唯一）
     */
    @Column(name = "coach_no", unique = true, nullable = false, length = 50)
    private String coachNo;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    /**
     * 性别：0未知 1男 2女
     */
    @Column(name = "gender")
    @Builder.Default
    private Integer gender = 0;

    /**
     * 联系电话 (也是登录账号)
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    // ================= 安全相关字段 (对应 ALTER TABLE 添加的列) =================

    /**
     * 登录密码哈希值
     * 对应 SQL: ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT ''
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    @Builder.Default
    private String passwordHash = "";

    // 【新增】标记是否为初始密码
    @Column(name = "is_initial_password", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Integer isInitialPassword = 1;

    /**
     * 角色 ID
     * 对应 SQL: ADD COLUMN role_id INT DEFAULT 3
     * 默认指向教练角色 (通常为 3)
     */
    @Column(name = "role_id")
    @Builder.Default
    private Integer roleId = 3;

    // ========================================================================

    /**
     * 教练类型：store(门店专属)/free(自由教练)
     */
    @Column(name = "coach_type", length = 20)
    @Builder.Default
    private String coachType = "store";

    /**
     * 所属门店ID
     * 门店专属教练必填，自由教练可为 null
     */
    @Column(name = "store_id")
    private Integer storeId;

    /**
     * 专长：瑜伽/力量/有氧/拳击/拉伸
     */
    @Column(name = "specialty", length = 100)
    private String specialty;

    /**
     * 教练等级：junior/middle/senior
     */
    @Column(name = "level", length = 20)
    @Builder.Default
    private String level = "junior";

    /**
     * 课时费（元/小时）
     */
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    /**
     * 个人介绍
     */
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    /**
     * 累计上课次数
     */
    @Column(name = "total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    /**
     * 状态：1在职 0离职 2休假
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 创建时间
     * 对应 SQL: DEFAULT CURRENT_TIMESTAMP
     * JPA 中通常由 @PrePersist 处理，或依赖数据库默认值
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 对应 SQL: ON UPDATE CURRENT_TIMESTAMP
     * JPA 中通常由 @PreUpdate 处理
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= 生命周期回调 =================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // 确保默认值在 Java 层也生效（虽然数据库有默认值，但双保险更好）
        if (this.gender == null) this.gender = 0;
        if (this.coachType == null) this.coachType = "store";
        if (this.level == null) this.level = "junior";
        if (this.totalSessions == null) this.totalSessions = 0;
        if (this.status == null) this.status = 1;
        if (this.passwordHash == null) this.passwordHash = "";
        if (this.roleId == null) this.roleId = 3;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= 辅助方法 =================

    public String getGenderText() {
        return switch (this.gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    public String getStatusText() {
        return switch (this.status) {
            case 1 -> "在职";
            case 0 -> "离职";
            case 2 -> "休假";
            default -> "未知";
        };
    }

    public String getLevelText() {
        return switch (this.level) {
            case "junior" -> "初级教练";
            case "middle" -> "中级教练";
            case "senior" -> "高级教练";
            default -> "未知等级";
        };
    }

    /**
     * 判断是否在职
     * 只有状态为 1 (在职) 才允许登录或排课
     */
    public boolean isActive() {
        return this.status == 1;
    }
}