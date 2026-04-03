package com.gym.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员到店记录实体 (对应表: member_visits)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member_visits") // 1. 修正表名
public class MemberVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    // 2. 修正类型：数据库是 DATE，Java 用 LocalDate
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_in_method", length = 20)
    @Builder.Default
    private String checkInMethod = "manual";

    @Column(name = "card_type", length = 20)
    @Builder.Default
    private String cardType = "period";

    @Column(name = "deduct_times")
    @Builder.Default
    private Integer deductTimes = 0;

    @Column(name = "related_appointment_id")
    private Integer relatedAppointmentId;

    @Column(name = "related_course_id")
    private Integer relatedCourseId;

    @Column(name = "staff_id")
    private Integer staffId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    // 3. 修正字段名：数据库是 created_at，不是 create_at
    // 4. 移除 update_time 和 deleted 字段，因为数据库表中不存在
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 如果希望由 Java 控制创建时间，保留 @PrePersist
    // 如果希望完全依赖数据库默认值 (DEFAULT CURRENT_TIMESTAMP)，可以去掉 @PrePersist 和此字段的插入逻辑
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        // 初始化默认值，防止 null
        if (this.checkInMethod == null) {
            this.checkInMethod = "manual";
        }
        if (this.cardType == null) {
            this.cardType = "period";
        }
        if (this.deductTimes == null) {
            this.deductTimes = 0;
        }
    }
}