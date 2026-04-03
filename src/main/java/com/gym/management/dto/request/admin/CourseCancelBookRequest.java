package com.gym.management.dto.request.admin;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CourseCancelBookRequest {

    @NotNull(message = "课程 ID 不能为空")
    private Integer courseId;

    @NotNull(message = "会员 ID 不能为空")
    private Integer memberId;

    @Size(max = 200, message = "取消原因最多 200 字符")
    private String cancelReason;
}