package com.gym.management.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 * 对应数据库表：employees
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 所属门店 ID
     * 注意：这里只存 ID，如果需要门店对象，可以使用 @ManyToOne 关联 Store 实体
     */
    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    /**
     * 员工工号 (唯一)
     */
    @Column(name = "employee_no", unique = true, nullable = false, length = 50)
    private String employeeNo;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    /**
     * 性别：0 未知 1 男 2 女
     */
    @Column(name = "gender", columnDefinition = "TINYINT DEFAULT 0")
    private Integer gender;

    /**
     * 联系电话
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 部门：前台/销售/后勤/管理
     */
    @Column(name = "department", length = 50)
    private String department;

    /**
     * 职位
     */
    @Column(name = "position", length = 50)
    private String position;

    /**
     * 入职日期
     */
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /**
     * 离职日期
     */
    @Column(name = "leave_date")
    private LocalDate leaveDate;

    /**
     * 基本工资
     */
    @Column(name = "base_salary", precision = 10, scale = 2)
    private BigDecimal baseSalary;

    /**
     * 状态：1 在职 0 离职 2 休假
     */
    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 创建时间 (数据库自动维护，通常设为只读)
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间 (数据库自动维护)
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= 辅助方法 (可选) =================

    /**
     * 判断员工是否在职
     */
    public boolean isActive() {
        return this.status != null && this.status == 1;
    }

    /**
     * 格式化性别显示
     */
    public String getGenderText() {
        if (this.gender == null) return "未知";
        switch (this.gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    /**
     * 格式化状态显示
     */
    public String getStatusText() {
        if (this.status == null) return "未知";
        switch (this.status) {
            case 1: return "在职";
            case 0: return "离职";
            case 2: return "休假";
            default: return "未知";
        }
    }
}