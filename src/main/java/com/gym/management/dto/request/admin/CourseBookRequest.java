package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CourseBookRequest {

    @NotNull(message = "课程 ID 不能为空")
    private Integer courseId;

    @NotNull(message = "会员 ID 不能为空")
    private Integer memberId;

    @Size(max = 100, message = "备注最多 100 字符")
    private String remark;
}