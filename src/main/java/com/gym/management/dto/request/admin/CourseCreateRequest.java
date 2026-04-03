package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseCreateRequest {

    @NotNull(message = "门店 ID 不能为空")
    private Integer storeId;

    private Integer coachId;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称最多 100 字符")
    private String courseName;

    @NotBlank(message = "课程类型不能为空")
    private String courseType; // 瑜伽/动感单车/搏击/舞蹈/力量

    private String courseLevel = "beginner"; // beginner/intermediate/advanced

    @Min(value = 1, message = "最大人数至少为 1")
    private Integer maxSeats = 20;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Min(value = 1, message = "课程时长至少 1 分钟")
    private Integer durationMinutes;

    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal price = BigDecimal.ZERO;

    @Size(max = 50, message = "教室最多 50 字符")
    private String room;

    private Integer status = 1;

    @Size(max = 500, message = "备注最多 500 字符")
    private String remark;
}