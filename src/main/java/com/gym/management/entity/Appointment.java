package com.gym.management.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "appointment_no", unique = true, nullable = false, length = 50)
    private String appointmentNo;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "coach_id", nullable = false)
    private Integer coachId;

    @Column(name = "time_slot_start", nullable = false)
    private LocalDateTime timeSlotStart;

    @Column(name = "time_slot_end", nullable = false)
    private LocalDateTime timeSlotEnd;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "coach_share", precision = 10, scale = 2)
    @ColumnDefault("0")
    private BigDecimal coachShare;

    @Column(name = "purpose", length = 100)
    private String purpose;

    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0待确认 1已确认 2已完成 3已取消 4已爽约

    @Column(name = "pay_status", columnDefinition = "TINYINT DEFAULT 0")
    private Integer payStatus; // 0未支付 1已支付

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "pay_method", columnDefinition = "TINYINT")
    private Integer payMethod; // 1微信 2支付宝 3现金

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmed_by")
    private Integer confirmedBy;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "cancel_by", columnDefinition = "TINYINT")
    private Integer cancelBy; // 1会员 2管理员

    @Column(name = "coach_check_in_time")
    private LocalDateTime coachCheckInTime;

    @Column(name = "coach_check_out_time")
    private LocalDateTime coachCheckOutTime;

    @Column(name = "actual_duration")
    private Integer actualDuration;

    @Column(name = "member_check_in_time")
    private LocalDateTime memberCheckInTime;

    @Column(name = "feedback_score")
    private Integer feedbackScore;

    @Column(name = "feedback_content", columnDefinition = "TEXT")
    private String feedbackContent;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
}