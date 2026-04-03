package com.gym.management.dto.request.member;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

// 创建预约请求
@Data
public class AppointmentCreateRequest {
    @NotNull(message = "门店 ID 不能为空")
    private Integer storeId;

    @NotNull(message = "教练 ID 不能为空")
    private Integer coachId;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotNull(message = "开始时间不能为空")
    private String startTime;  // 格式："10:00:00"

    @NotNull(message = "结束时间不能为空")
    private String endTime;    // 格式："11:00:00"

    private String purpose; // 预约目的

    // 辅助方法：获取完整的开始时间
    public LocalDateTime getStartDateTime() {
        if (this.date == null || this.startTime == null) {
            return null;
        }
        return java.time.LocalDateTime.of(this.date, java.time.LocalTime.parse(this.startTime));
    }

    // 辅助方法：获取完整的结束时间
    public LocalDateTime getEndDateTime() {
        if (this.date == null || this.endTime == null) {
            return null;
        }
        return java.time.LocalDateTime.of(this.date, java.time.LocalTime.parse(this.endTime));
    }

    // Getters and Setters
}