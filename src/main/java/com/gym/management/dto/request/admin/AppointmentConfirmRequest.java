package com.gym.management.dto.request.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

// 管理员确认预约请求
@Data
public class AppointmentConfirmRequest {
    private String remark; // 确认备注

    // Getters and Setters
}