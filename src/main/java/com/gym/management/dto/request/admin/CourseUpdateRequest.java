package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseUpdateRequest {

    @Size(max = 100, message = "课程名称最多 100 字符")
    private String courseName;

    private String courseType;
    private String courseLevel;

    @Min(value = 1, message = "最大人数至少为 1")
    private Integer maxSeats;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Min(value = 1, message = "课程时长至少 1 分钟")
    private Integer durationMinutes;

    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal price;

    @Size(max = 50, message = "教室最多 50 字符")
    private String room;

    private Integer status;

    @Size(max = 500, message = "备注最多 500 字符")
    private String remark;

    private Integer coachId;
}