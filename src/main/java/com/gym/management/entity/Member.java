package com.gym.management.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员实体类
 * 对应数据库表：members
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "members")
@Slf4j
public class Member {

    /**
     * 会员ID (主键)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 会员卡号（全系统唯一）
     */
    @Column(name = "member_no", unique = true, nullable = false, length = 50)
    private String memberNo;

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

    /**
     * 出生日期
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * 注册门店ID（首次办卡门店）
     */
    @Column(name = "register_store_id")
    private Integer registerStoreId;

    /**
     * 入会日期
     */
    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    /**
     * 会员到期日期
     */
    @Column(name = "expire_date")
    private LocalDate expireDate;

    /**
     * 卡类型：period(期限卡)/times(次卡)/vip(贵宾卡)
     */
    @Column(name = "card_type", length = 20)
    @Builder.Default
    private String cardType = "period";

    /**
     * 总次数（次卡用）
     */
    @Column(name = "total_times")
    @Builder.Default
    private Integer totalTimes = 0;

    /**
     * 剩余次数（次卡用）
     */
    @Column(name = "remaining_times")
    @Builder.Default
    private Integer remainingTimes = 0;

    /**
     * 账户余额
     */
    @Column(name = "balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 累计消费金额
     */
    @Column(name = "total_consumption", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalConsumption = BigDecimal.ZERO;

    /**
     * 总到店次数
     */
    @Column(name = "visit_count")
    @Builder.Default
    private Integer visitCount = 0;

    /**
     * 最后到店时间
     */
    @Column(name = "last_visit_at")
    private LocalDateTime lastVisitAt;

    /**
     * 最后到店门店ID
     */
    @Column(name = "last_visit_store_id")
    private Integer lastVisitStoreId;

    /**
     * 状态：1正常 0过期 2冻结
     */
    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================= 安全相关字段 (已添加 @Column 映射) =================

    /**
     * 登录密码哈希值
     * 注意：不要设置 length 太小，BCrypt 加密后通常是 60 字符
     */

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    // 【新增】标记是否为初始密码
    @Column(name = "is_initial_password", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Integer isInitialPassword = 1;

    /**
     * 角色 ID (对应 roles 表 id)
     * 会员通常有一个默认角色，如 'ROLE_MEMBER'
     */
    @Column(name = "role_id")
    private Integer roleId;

    // ================= 生命周期回调 =================

    /**
     * 持久化前回调：设置默认时间和初始值
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.gender == null) this.gender = 0;
        if (this.cardType == null) this.cardType = "period";
        if (this.totalTimes == null) this.totalTimes = 0;
        if (this.remainingTimes == null) this.remainingTimes = 0;
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        if (this.totalConsumption == null) this.totalConsumption = BigDecimal.ZERO;
        if (this.visitCount == null) this.visitCount = 0;
        if (this.status == null) this.status = 1;

        // 【重要】如果是新注册会员，确保有默认角色ID (如果业务逻辑没传的话)
        // 这里不做强制赋值，建议在 Service 层处理，避免硬编码
    }

    /**
     * 更新前回调：更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= 辅助方法 =================

    public String getGenderText() {
        if (this.gender == null) return "未知";
        return switch (this.gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    public String getStatusText() {
        if (this.status == null) return "未知";
        return switch (this.status) {
            case 1 -> "正常";
            case 0 -> "过期";
            case 2 -> "冻结";
            default -> "未知";
        };
    }

    /**
     * 检查会员是否过期
     * 用于业务逻辑判断，不直接控制登录状态（登录状态由 status 字段控制）
     */
    public boolean isExpired() {
        if (this.expireDate == null) return false;
        return LocalDate.now().isAfter(this.expireDate);
    }
}