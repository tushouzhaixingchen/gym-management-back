package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
public class CourseQueryRequest {

    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为 1")
    @Max(value = 100, message = "每页数量最大为 100")
    private Integer size = 10;

    private Integer storeId;
    private Integer coachId;
    private String courseName;
    private String courseType;
    private String courseLevel;
    private Integer status;
    private LocalDateTime startTimeStart;
    private LocalDateTime startTimeEnd;
}