package com.gym.management.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 管理员端预约列表响应（包含更多关联信息）
@Data
public class AdminAppointmentResponse {
    private Integer id;
    private String appointmentNo;       // 预约编号
    private Integer memberId;           // 会员 ID
    private String memberName;          // 会员名
    private String memberPhone;         // 会员电话（方便联系）
    private Integer coachId;            // 教练 ID
    private String coachName;           // 教练名
    private Integer storeId;            // 门店 ID
    private String storeName;           // 门店名称
    private LocalDateTime timeSlotStart;  // 开始时间
    private LocalDateTime timeSlotEnd;    // 结束时间
    private Integer durationMinutes;      // 时长（分钟）
    private BigDecimal price;             // 价格
    private String purpose;               // 预约目的
    private Integer status;               // 状态：0 待确认 1 已确认 2 已完成 3 已取消 4 已爽约
    private String statusDesc;            // 状态描述
    private Integer payStatus;            // 支付状态：0 未支付 1 已支付
    private String payStatusDesc;         // 支付状态描述
    private LocalDateTime payTime;        // 支付时间
    private Integer payMethod;            // 支付方式
    private LocalDateTime createdAt;      // 创建时间
    private LocalDateTime confirmedAt;    // 确认时间
    private String confirmedByAdminName;  // 确认人姓名
    private String cancelReason;          // 取消原因
}