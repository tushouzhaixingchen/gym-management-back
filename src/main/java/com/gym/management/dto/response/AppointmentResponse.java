package com.gym.management.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 预约详情响应
@Data
public class AppointmentResponse {
    private Integer id;
    private String appointmentNo;
    private Integer coachId;      // 教练 ID
    private String coachName;     // 教练姓名
    private Integer storeId;      // 门店 ID
    private String storeName;     // 门店名称
    private LocalDateTime timeSlotStart;  // 开始时间
    private LocalDateTime timeSlotEnd;    // 结束时间
    private Integer durationMinutes;      // 时长（分钟）
    private BigDecimal price;             // 价格
    private String purpose;               // 预约目的
    private Integer status;               // 状态：0 待确认 1 已确认 2 已完成 3 已取消 4 已爽约
    private String statusDesc;            // 状态描述
    private Integer payStatus;            // 支付状态：0 未支付 1 已支付
    private String payStatusDesc;         // 支付状态描述
    private LocalDateTime createdAt;      // 创建时间
    private String cancelReason;          // 取消原因

    // Getters and Setters
}